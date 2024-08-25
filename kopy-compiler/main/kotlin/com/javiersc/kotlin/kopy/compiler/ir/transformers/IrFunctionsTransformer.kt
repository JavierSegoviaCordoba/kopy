@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.firstIrClass
import com.javiersc.kotlin.kopy.KopyFunctionCopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.KopyFunctionUpdateEach
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyCopy
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyCopyOrInvoke
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopySet
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdate
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdateEach
import kotlinx.atomicfu.AtomicRef
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
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

internal class IrFunctionsTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    private val kopyOptInClassId: ClassId = "com.javiersc.kotlin.kopy.KopyOptIn".toClassId()
    private val kopyFunctionCopyClassId: ClassId = classId<KopyFunctionCopy>()
    private val kopyFunctionInvokeClassId: ClassId = classId<KopyFunctionInvoke>()
    private val kopyFunctionSetClassId: ClassId = classId<KopyFunctionSet>()
    private val kopyFunctionUpdateClassId: ClassId = classId<KopyFunctionUpdate>()
    private val kopyFunctionUpdateEachClassId: ClassId = classId<KopyFunctionUpdateEach>()
    private val atomicRefClassId: ClassId = "kotlinx.atomicfu.AtomicRef".toClassId()
    private val atomicName: Name = "_atomic".toName()
    private val valueName: Name = "value".toName()
    private val copyName: Name = "copy".toName()
    private val invokeName: Name = "invoke".toName()
    private val setName: Name = "set".toName()
    private val updateName: Name = "update".toName()
    private val updateEachName: Name = "updateEach".toName()

    private val unitType: IrType = pluginContext.irBuiltIns.unitType

    private val function1Class: IrClass = pluginContext.irBuiltIns.functionN(1)

    private val mapCallableId: CallableId = "kotlin.collections.map".toCallableId()
    private val mapFunction: IrSimpleFunction
        get() = pluginContext
            .referenceFunctions(mapCallableId)
            .firstIsInstance<IrSimpleFunctionSymbol>()
            .owner

    private val listClass: IrClass =
        pluginContext.referenceClass("kotlin.collections.List".toClassId())!!.owner

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFunction(): IrStatement = super.visitSimpleFunction(declaration)

        return when {
            declaration.isKopyCopyOrInvoke -> transformCopyOrInvokeFunction(declaration)
            declaration.isKopySet -> transformSetFunction(declaration)
            declaration.isKopyUpdate -> transformUpdateFunction(declaration)
            declaration.isKopyUpdateEach -> transformUpdateEachFunction(declaration)
            runCatching { declaration.parentAsClass }.isFailure -> originalFunction()
            !declaration.parentAsClass.isData -> originalFunction()
            else -> originalFunction()
        }
    }

    private fun transformCopyOrInvokeFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        declaration.body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
            val thisCopyIrVariable: IrVariable = thisCopyIrVariable(declaration)
            +copyCall(declaration, thisCopyIrVariable)
            +atomicValueIrReturn(declaration, thisCopyIrVariable)
        }
        return declaration
    }

    private fun transformSetFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        val function: IrSimpleFunction =
            declaration.apply {
                body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                    val unitClass: IrClassSymbol = pluginContext.irBuiltIns.unitClass.owner.symbol
                    val unitCall: IrGetObjectValue = irGetObject(unitClass)
                    val irReturn: IrReturn = irReturn(unitCall)
                    +irReturn
                }
            }
        return function
    }

    private fun transformUpdateFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        val function: IrSimpleFunction =
            declaration.apply {
                body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                    val thisGet: IrGetValue = irGet(extensionReceiverParameter!!)
                    val transformValueParameter: IrValueParameter = valueParameters.first()
                    val func1Invoke: IrSimpleFunction =
                        function1Class.findDeclaration<IrSimpleFunction> {
                            it.name == invokeName
                        }!!

                    val transformCallDispatchReceiver: IrGetValue =
                        irGet(transformValueParameter).apply {
                            origin = IrStatementOrigin.VARIABLE_AS_FUNCTION
                        }

                    val transformCall: IrMemberAccessExpression<*> = irCallOp(
                        callee = func1Invoke.symbol,
                        type = declaration.typeParameters.first().defaultType,
                        dispatchReceiver = transformCallDispatchReceiver,
                        argument = thisGet,
                        origin = IrStatementOrigin.INVOKE,
                    )
                    +typeOperator(
                        resultType = unitType,
                        argument = transformCall,
                        typeOperator = IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                        typeOperand = unitType,
                    )
                }
            }
        return function
    }

    private fun transformUpdateEachFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        val function: IrSimpleFunction =
            declaration.apply {
                body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                    val thisGet: IrGetValue = irGet(extensionReceiverParameter!!)
                    val transformValueParameter: IrValueParameter = valueParameters.first()

                    val transformCallType: IrSimpleType =
                        listClass.typeWith(typeParameters.map { it.defaultType })

                    val typeParameterType: IrSimpleType =
                        declaration.typeParameters.map { it.defaultType }.first()

                    val transformCall: IrCall =
                        irCall(callee = mapFunction.symbol).apply {
                            this.putTypeArgument(0, typeParameterType)
                            this.putTypeArgument(1, typeParameterType)
                            this.extensionReceiver = thisGet
                            this.putValueArgument(0, irGet(transformValueParameter))
                            this.type = transformCallType
                        }

                    +typeOperator(
                        resultType = unitType,
                        argument = transformCall,
                        typeOperator = IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                        typeOperand = unitType,
                    )
                }
            }
        return function
    }

    private fun IrBlockBodyBuilder.thisCopyIrVariable(declaration: IrSimpleFunction): IrVariable {
        val copyFun: IrSimpleFunction =
            declaration.parentAsClass.findDeclaration<IrSimpleFunction> {
                val isKopyCopyFun: Boolean = it.isKopyCopy
                it.name == copyName && !isKopyCopyFun
            }!!
        val receiver: IrGetValue = irGet(declaration.dispatchReceiverParameter!!)
        val copyCall: IrMemberAccessExpression<*> =
            irCall(copyFun.symbol).apply {
                dispatchReceiver = receiver
            }

        val irVariable: IrVariable = irTemporary(copyCall)
        return irVariable
    }

    private fun IrBlockBodyBuilder.copyCall(
        declaration: IrSimpleFunction,
        thisCopyIrVariable: IrVariable,
    ): IrFunctionAccessExpression {
        val invokeFunc: IrSimpleFunction =
            function1Class.findDeclaration<IrSimpleFunction> { it.name == invokeName }!!

        val copyValueParameter: IrValueParameter = declaration.valueParameters.first()
        val thisCopyIrVariableGetValue: IrGetValue = irGet(thisCopyIrVariable)

        val copyCall: IrCall = irCall(invokeFunc.symbol, unitType).apply {
            dispatchReceiver = irGet(copyValueParameter).apply {
                origin = IrStatementOrigin.VARIABLE_AS_FUNCTION
            }
            putValueArgument(0, thisCopyIrVariableGetValue)
            origin = IrStatementOrigin.INVOKE
        }
        return copyCall
    }

    private fun IrBlockBodyBuilder.atomicValueIrReturn(
        declaration: IrSimpleFunction,
        thisCopyIrVariable: IrVariable,
    ): IrReturn =
        pluginContext.declarationIrBuilder(declaration.symbol).run {
            val thisCopyGetValue: IrGetValue = irGet(thisCopyIrVariable)
            val atomicRefClassId: ClassId = classId<AtomicRef<*>>()
            val atomicProperty = declaration.parent.asIr<IrClass>()
                .findDeclaration<IrProperty> { it.name == atomicName }

            val getAtomicCall = irCall(atomicProperty!!.getter!!).apply {
                dispatchReceiver = thisCopyGetValue
            }

            val atomicValueGetter = pluginContext.firstIrClass(atomicRefClassId)
                .findDeclaration<IrProperty> { it.name == valueName }!!.getter!!

            val atomicValueGetterCall = irCall(atomicValueGetter).apply {
                dispatchReceiver = getAtomicCall
            }
            val irReturn: IrReturn = irReturn(atomicValueGetterCall)
            irReturn
        }
}