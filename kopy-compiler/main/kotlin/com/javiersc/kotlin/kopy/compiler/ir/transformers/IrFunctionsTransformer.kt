@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.ir.DeclarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.extensionReceiver
import com.javiersc.kotlin.compiler.extensions.ir.firstIrClass
import com.javiersc.kotlin.compiler.extensions.ir.regularParameters
import com.javiersc.kotlin.kopy.compiler.atomicReferenceClassId
import com.javiersc.kotlin.kopy.compiler.copyName
import com.javiersc.kotlin.kopy.compiler.invokeName
import com.javiersc.kotlin.kopy.compiler.ir.utils.insertExtensionReceiver
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyCopy
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyCopyOrInvoke
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopySet
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdate
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdateEach
import com.javiersc.kotlin.kopy.compiler.listClassId
import com.javiersc.kotlin.kopy.compiler.loadName
import com.javiersc.kotlin.kopy.compiler.mapCallableId
import com.javiersc.kotlin.kopy.compiler.underscoreAtomicName
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallOp
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.typeOperator
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

internal class IrFunctionsTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    private val unitType: IrType = pluginContext.irBuiltIns.unitType

    private val function1Class: IrClass = pluginContext.irBuiltIns.functionN(1)

    private val mapFunction: IrSimpleFunction
        get() =
            pluginContext
                .finderForBuiltins()
                .findFunctions(mapCallableId)
                .firstIsInstance<IrSimpleFunctionSymbol>()
                .owner

    private val listClass: IrClass =
        pluginContext.finderForBuiltins().findClass(listClassId)!!.owner

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFunction(): IrStatement = super.visitSimpleFunction(declaration)
        return context(pluginContext) {
            when {
                declaration.isKopyCopyOrInvoke -> transformCopyOrInvokeFunction(declaration)
                declaration.isKopySet -> transformSetFunction(declaration)
                declaration.isKopyUpdate -> transformUpdateFunction(declaration)
                declaration.isKopyUpdateEach -> transformUpdateEachFunction(declaration)
                runCatching { declaration.parentAsClass }.isFailure -> originalFunction()
                !declaration.parentAsClass.isData -> originalFunction()
                else -> originalFunction()
            }
        }
    }

    context(context: IrPluginContext)
    private fun transformCopyOrInvokeFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        declaration.body = declaration.createCopyOrInvokeBody()
        return declaration
    }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createCopyOrInvokeBody() =
        DeclarationIrBuilder(symbol).irBlockBody {
            val thisCopyIrVariable: IrVariable =
                createTemporaryCopyVariable(this@createCopyOrInvokeBody)
            +callCopyLambda(this@createCopyOrInvokeBody, thisCopyIrVariable)
            +returnAtomicValue(this@createCopyOrInvokeBody, thisCopyIrVariable)
        }

    context(context: IrPluginContext)
    private fun transformSetFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        declaration.body = declaration.createReturnUnitBody()
        return declaration
    }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createReturnUnitBody() =
        DeclarationIrBuilder(symbol).irBlockBody {
            val unitClass: IrClassSymbol = context.irBuiltIns.unitClass.owner.symbol
            val unitCall: IrGetObjectValue = irGetObject(unitClass)
            +irReturn(unitCall)
        }

    context(context: IrPluginContext)
    private fun transformUpdateFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        declaration.body = declaration.createUpdateBody()
        return declaration
    }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createUpdateBody() =
        DeclarationIrBuilder(symbol).irBlockBody {
            val transformCall: IrMemberAccessExpression<*> =
                createTransformCall(this@createUpdateBody)
            +coerceToUnit(transformCall)
        }

    private fun IrBlockBodyBuilder.createTransformCall(
        declaration: IrSimpleFunction,
    ): IrMemberAccessExpression<*> {
        val thisGet: IrGetValue = irGet(declaration.extensionReceiver!!)
        val transformValueParameter: IrValueParameter = declaration.regularParameters.first()
        val invokeFunction: IrSimpleFunction = function1InvokeFunction()
        val transformCallDispatchReceiver: IrGetValue =
            irGetAsVariableFunction(transformValueParameter)

        return irCallOp(
            callee = invokeFunction.symbol,
            type = declaration.typeParameters.first().defaultType,
            dispatchReceiver = transformCallDispatchReceiver,
            argument = thisGet,
            origin = IrStatementOrigin.INVOKE,
        )
    }

    context(context: IrPluginContext)
    private fun transformUpdateEachFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        declaration.body = declaration.createUpdateEachBody()
        return declaration
    }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createUpdateEachBody() =
        DeclarationIrBuilder(symbol).irBlockBody {
            val mapCall: IrCall = createMapCall(this@createUpdateEachBody)
            +coerceToUnit(mapCall)
        }

    private fun IrBlockBodyBuilder.createMapCall(declaration: IrSimpleFunction): IrCall {
        val thisGet: IrGetValue = irGet(declaration.extensionReceiver!!)
        val transformValueParameter: IrValueParameter = declaration.regularParameters.first()
        val typeParameterType: IrSimpleType = declaration.firstDefaultTypeParameter()

        return irCall(callee = mapFunction.symbol).apply {
            typeArguments[0] = typeParameterType
            typeArguments[1] = typeParameterType
            insertExtensionReceiver(thisGet)
            arguments[transformValueParameter.indexInParameters - 1] =
                irGet(transformValueParameter)
            type = declaration.listOfDefaultTypeParameters()
        }
    }

    private fun IrBlockBodyBuilder.coerceToUnit(expression: IrMemberAccessExpression<*>) =
        typeOperator(
            resultType = unitType,
            argument = expression,
            typeOperator = IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
            typeOperand = unitType,
        )

    private fun IrBlockBodyBuilder.createTemporaryCopyVariable(
        declaration: IrSimpleFunction,
    ): IrVariable {
        val copyFun: IrSimpleFunction = declaration.parentAsClass.kotlinDataClassCopyFunction()
        val receiver: IrGetValue = irGet(declaration.dispatchReceiverParameter!!)
        val copyCall: IrMemberAccessExpression<*> =
            irCall(copyFun.symbol).apply { dispatchReceiver = receiver }

        val irVariable: IrVariable = irTemporary(copyCall)
        return irVariable
    }

    private fun IrClass.kotlinDataClassCopyFunction(): IrSimpleFunction =
        findDeclaration {
            val isKopyCopyFun: Boolean = it.isKopyCopy
            it.name == copyName && !isKopyCopyFun
        }!!

    private fun IrBlockBodyBuilder.callCopyLambda(
        declaration: IrSimpleFunction,
        thisCopyIrVariable: IrVariable,
    ): IrFunctionAccessExpression {
        val invokeFunc: IrSimpleFunction = function1InvokeFunction()
        val copyValueParameter: IrValueParameter = declaration.regularParameters.first()
        val thisCopyIrVariableGetValue: IrGetValue = irGet(thisCopyIrVariable)

        val copyCall: IrCall =
            irCall(invokeFunc.symbol, unitType).apply {
                dispatchReceiver =
                    irGet(copyValueParameter).apply {
                        origin = IrStatementOrigin.VARIABLE_AS_FUNCTION
                    }
                arguments[copyValueParameter.indexInParameters] = thisCopyIrVariableGetValue
                origin = IrStatementOrigin.INVOKE
            }
        return copyCall
    }

    private fun IrBlockBodyBuilder.irGetAsVariableFunction(
        valueParameter: IrValueParameter
    ): IrGetValue = irGet(valueParameter).apply { origin = IrStatementOrigin.VARIABLE_AS_FUNCTION }

    private fun function1InvokeFunction(): IrSimpleFunction =
        function1Class.findDeclaration<IrSimpleFunction> { it.name == invokeName }!!

    private fun IrSimpleFunction.firstDefaultTypeParameter(): IrSimpleType =
        typeParameters.map { it.defaultType }.first()

    private fun IrSimpleFunction.listOfDefaultTypeParameters(): IrSimpleType =
        listClass.typeWith(typeParameters.map { it.defaultType })

    context(context: IrPluginContext)
    private fun returnAtomicValue(
        declaration: IrSimpleFunction,
        thisCopyIrVariable: IrVariable,
    ): IrReturn =
        DeclarationIrBuilder(declaration.symbol).run {
            val thisCopyGetValue: IrGetValue = irGet(thisCopyIrVariable)
            val atomicProperty: IrProperty = declaration.parent.asIr<IrClass>().atomicProperty()
            val getAtomicCall: IrFunctionAccessExpression =
                irCall(atomicProperty.getter!!).apply { dispatchReceiver = thisCopyGetValue }
            val atomicReferenceLoadFunction: IrFunction = atomicReferenceLoadFunction()
            val atomicReferenceLoadCall: IrFunctionAccessExpression =
                irCall(atomicReferenceLoadFunction).apply { dispatchReceiver = getAtomicCall }
            irReturn(atomicReferenceLoadCall)
        }

    context(context: IrPluginContext)
    private fun atomicReferenceLoadFunction(): IrFunction =
        firstIrClass(atomicReferenceClassId).atomicReferenceLoadFunction()

    private fun IrClass.atomicProperty(): IrProperty =
        findDeclaration<IrProperty> { it.name == underscoreAtomicName }!!

    private fun IrClass.atomicReferenceLoadFunction(): IrFunction =
        findDeclaration<IrFunction> { it.name == loadName }!!
}
