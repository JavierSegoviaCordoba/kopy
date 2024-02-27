package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.firstIrSimpleFunction
import com.javiersc.kotlin.kopy.compiler.ir._internal.utils.findDeclarationParent
import com.javiersc.kotlin.kopy.compiler.ir._internal.utils.isKopySetOrUpdate
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.name.SpecialNames

internal class IrSetOrUpdateCallTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        fun originalCall(): IrExpression = super.visitCall(expression)
        if (!expression.isKopySetOrUpdate) return originalCall()

        val alsoCall: IrCall = createAlsoCall(expression) ?: return originalCall()
        val originalCallCallWithAlsoCall: IrCall =
            expression.apply { putValueArgument(valueArgumentsCount - 1, alsoCall) }
        return originalCallCallWithAlsoCall
    }

    private fun createAlsoCall(expression: IrCall): IrCall? {
        val alsoItValueParameter: IrExpression =
            expression.getValueArgument((expression.valueArgumentsCount - 1).coerceAtLeast(0))
                ?: return null
        val expressionParent: IrDeclarationParent =
            expression.findDeclarationParent(moduleFragment)?.asIrOrNull<IrDeclarationParent>()
                ?: return null
        val setOrUpdateType: IrType = expression.extensionReceiver?.type ?: return null
        val alsoFunction: IrSimpleFunction =
            pluginContext.firstIrSimpleFunction("kotlin.also".toCallableId())

        val alsoBlockFunction: IrSimpleFunction =
            pluginContext.irFactory
                .buildFun {
                    name = SpecialNames.ANONYMOUS
                    visibility = DescriptorVisibilities.LOCAL
                    returnType = pluginContext.irBuiltIns.unitType
                    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                }
                .apply {
                    parent = expressionParent
                    addValueParameter {
                        this.name = StandardNames.IMPLICIT_LAMBDA_PARAMETER_NAME
                        this.type = setOrUpdateType
                    }

                    val alsoBodyBlock: IrBlockBody =
                        pluginContext.declarationIrBuilder(this.symbol).irBlockBody {
                            val itValueParameter: IrValueParameter = valueParameters.first()
                            val alsoItValueParameterGetValue: IrGetValue = irGet(itValueParameter)
                            val setter: IrSimpleFunction =
                                expression.extensionReceiver
                                    ?.asIrOrNull<IrCall>()
                                    ?.symbol
                                    ?.owner
                                    ?.correspondingPropertySymbol
                                    ?.owner
                                    ?.setter ?: return@irBlockBody
                            val setterDispatchReceiver: IrExpression? =
                                expression.extensionReceiver
                                    ?.asIrOrNull<IrCall>()
                                    ?.dispatchReceiver
                                    ?.deepCopyWithSymbols()
                            val setCall: IrCall =
                                irSet(
                                    type = pluginContext.irBuiltIns.unitType,
                                    receiver = setterDispatchReceiver,
                                    setterSymbol = setter.symbol,
                                    value = alsoItValueParameterGetValue,
                                )
                            +setCall
                        }

                    body = alsoBodyBlock
                }

        val alsoCall: IrCall =
            pluginContext.declarationIrBuilder(alsoFunction.symbol).run {
                irCall(alsoFunction.symbol).apply {
                    type = alsoItValueParameter.type

                    extensionReceiver = run {
                        val index: Int = (expression.valueArgumentsCount - 1).coerceAtLeast(0)
                        expression.getValueArgument(index)
                    }
                    putTypeArgument(0, setOrUpdateType)
                    val alsoBlockFunctionExpression: IrFunctionExpression =
                        createIrFunctionExpression(
                            type =
                                pluginContext.irBuiltIns.run {
                                    functionN(1).typeWith(setOrUpdateType, unitType)
                                },
                            function = alsoBlockFunction,
                            origin = IrStatementOrigin.LAMBDA,
                        )
                    putValueArgument(index = 0, valueArgument = alsoBlockFunctionExpression)
                }
            }

        return alsoCall
    }
}
