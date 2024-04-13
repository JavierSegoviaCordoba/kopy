package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrBlockBody
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.createLambdaIrSimpleFunction
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.firstIrClassSymbolOrNull
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.compiler.ir.utils.findDeclarationParent
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdateEach
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.statements

internal class IrUpdateEachCallTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        fun original(): IrExpression = super.visitCall(expression)

        if (!expression.isKopyUpdateEach) return original()

        val updateCall: IrFunctionAccessExpression =
            pluginContext.createUpdateCall(expression) ?: return original()
        return updateCall
    }

    private fun IrPluginContext.createUpdateCall(expression: IrCall): IrFunctionAccessExpression? {
        val updateFunctionSymbol: IrSimpleFunctionSymbol =
            expression.dispatchReceiver?.type?.classOrNull?.functions?.firstOrNull {
                it.owner.asIrOrNull<IrElement>()?.hasAnnotation<KopyFunctionUpdate>() == true
            } ?: return null
        val updateFunction: IrSimpleFunction = updateFunctionSymbol.owner
        val expressionExtensionReceiverType: IrType =
            expression.extensionReceiver?.type ?: return null
        val kFunction1Type: IrType =
            irBuiltIns
                .functionN(1)
                .typeWith(expressionExtensionReceiverType, expressionExtensionReceiverType)
        val expressionParent: IrDeclarationParent =
            expression.findDeclarationParent(moduleFragment)?.asIrOrNull<IrDeclarationParent>()
                ?: return null
        val updateCall: IrFunctionAccessExpression =
            declarationIrBuilder(updateFunction.symbol).irCall(updateFunction).also {
                it.dispatchReceiver = expression.dispatchReceiver
                it.extensionReceiver = expression.extensionReceiver
                it.type = expression.type
                it.putTypeArgument(0, it.extensionReceiver?.type)
                val lambda = createLambdaIrSimpleFunction {
                    this.parent = expressionParent
                    this.addValueParameter("it", expressionExtensionReceiverType)
                    this.returnType = expressionExtensionReceiverType
                }
                val lambdaFunctionExpression: IrFunctionExpression =
                    createIrFunctionExpression(
                        type = kFunction1Type,
                        function = lambda,
                        origin = IrStatementOrigin.LAMBDA,
                    )
                it.putValueArgument(0, lambdaFunctionExpression)
                val mapCall = createMapCallToUpdateCall(expression, it) ?: return null
                lambda.body = createIrBlockBody {
                    this.statements.add(declarationIrBuilder(lambda.symbol).irReturn(mapCall))
                }
            }
        return updateCall
    }

    private fun IrPluginContext.createMapCallToUpdateCall(
        expression: IrCall,
        updateCall: IrFunctionAccessExpression,
    ): IrFunctionAccessExpression? {
        val mapFunction: IrSimpleFunction = iterableMapFunctionOrNull ?: return null

        val expressionLambda: IrFunctionExpression? =
            expression.getValueArgument(0)?.asIrOrNull<IrFunctionExpression>()

        val valueArgumentFromUpdate: IrSimpleFunction =
            updateCall.getValueArgument(0)?.asIrOrNull<IrFunctionExpression>()?.function
                ?: return null
        val itValueParameterFromUpdate: IrValueParameter =
            valueArgumentFromUpdate.valueParameters.firstOrNull() ?: return null
        val mapCallType: IrType = itValueParameterFromUpdate.type

        val mapCall: IrFunctionAccessExpression =
            declarationIrBuilder(mapFunction.symbol)
                .irCall(callee = mapFunction.symbol, type = mapCallType)
                .also {
                    it.extensionReceiver =
                        declarationIrBuilder(it.symbol).irGet(itValueParameterFromUpdate)
                    val typeArg = expression.getTypeArgument(0)!!
                    it.putTypeArgument(0, typeArg)
                    it.putTypeArgument(1, typeArg)
                    val lambdaParent: IrSimpleFunction =
                        updateCall.getValueArgument(0)?.asIrOrNull<IrFunctionExpression>()?.function
                            ?: return null
                    val lambda = createLambdaIrSimpleFunction {
                        this.parent = lambdaParent
                        this.addValueParameter("it", typeArg)
                        this.returnType = typeArg
                        val originalBody: IrBody? =
                            expressionLambda?.deepCopyWithSymbols(parent)?.function?.body
                        val originalStatements: List<IrStatement> =
                            originalBody?.statements.orEmpty()
                        this.body =
                            declarationIrBuilder(this).irBlockBody {
                                for (original in originalStatements) {
                                    val statement: IrStatement =
                                        if (original is IrReturn) irReturn(original.value)
                                        else original
                                    +statement
                                }
                            }
                    }
                    val funcExp: IrFunctionExpression =
                        createIrFunctionExpression(
                            type = irBuiltIns.functionN(1).typeWith(typeArg, typeArg),
                            function = lambda,
                            origin = IrStatementOrigin.LAMBDA,
                        )
                    it.putValueArgument(0, funcExp)
                }
        return mapCall
    }

    private val IrPluginContext.iterableMapFunctionOrNull: IrSimpleFunction?
        get() =
            referenceFunctions("kotlin.collections.map".toCallableId())
                .firstOrNull {
                    val extensionReceiverClass: IrClassSymbol? =
                        it.owner.extensionReceiverParameter?.type?.classOrNull
                    if (extensionReceiverClass == null || iterableClassSymbolOrNull == null) false
                    else extensionReceiverClass == iterableClassSymbolOrNull
                }
                ?.owner

    private val IrPluginContext.iterableClassSymbolOrNull: IrClassSymbol?
        get() = firstIrClassSymbolOrNull(classId<Iterable<*>>())
}