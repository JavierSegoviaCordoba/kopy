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
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.SpecialNames

internal class SetOrUpdateCallTransformer(
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

                    body =
                        pluginContext.declarationIrBuilder(this.symbol).irBlockBody {
                            val itValueParameter: IrValueParameter = valueParameters.first()
                            val alsoItValueParameterGetValue: IrGetValue = irGet(itValueParameter)
                            val setKopyableReferenceCall: IrCall? =
                                createSetKopyableReferenceCall(
                                    originalExpression = expression,
                                    alsoItValueParameterGetValue = alsoItValueParameterGetValue
                                )
                            if (setKopyableReferenceCall != null) +setKopyableReferenceCall
                        }
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

    private fun createSetKopyableReferenceCall(
        originalExpression: IrCall,
        alsoItValueParameterGetValue: IrGetValue,
    ): IrCall? {
        val kopyClassType: IrType = originalExpression.dispatchReceiver?.type ?: return null
        val kopyableGetValue: IrGetValue =
            originalExpression.asIrOrNull<IrCall>()?.getValueArgument(0)?.asIrOrNull<IrGetValue>()
                ?: return null
        val kopyableValueDeclaration: IrValueDeclaration = kopyableGetValue.symbol.owner

        val kopyableScopeClass: IrClassSymbol = kopyableGetValue.type.classOrFail
        val getKopyableReferenceFunction: IrSimpleFunctionSymbol =
            kopyableScopeClass.getSimpleFunction("getKopyableReference")!!

        val getKopyableReferenceCall: IrCall =
            pluginContext.declarationIrBuilder(kopyableGetValue.symbol).run {
                irCall(getKopyableReferenceFunction).apply {
                    dispatchReceiver = irGet(kopyableValueDeclaration)
                    type = kopyClassType
                }
            }

        val propertyGetFunction: IrSimpleFunction =
            originalExpression.extensionReceiver.asIrOrNull<IrCall>()?.symbol?.owner ?: return null

        val kopyClass = kopyClassType.classOrFail
        val kopyClassCopyFunctionSymbol = kopyClass.owner.getSimpleFunction("copy")!!

        val copyCall: IrCall =
            pluginContext.declarationIrBuilder(originalExpression.symbol).run {
                irCall(kopyClassCopyFunctionSymbol).apply {
                    dispatchReceiver = getKopyableReferenceCall

                    val copyParamIndex: Int =
                        kopyClassCopyFunctionSymbol.owner.valueParameters.indexOfFirst {
                            it.name == propertyGetFunction.correspondingPropertySymbol?.owner?.name
                        }
                    putValueArgument(copyParamIndex, alsoItValueParameterGetValue)
                }
            }

        val setKopyableReferenceFunction: IrSimpleFunctionSymbol =
            kopyableScopeClass.getSimpleFunction("setKopyableReference")!!

        val setKopyableReferenceCall: IrCall =
            pluginContext.declarationIrBuilder(kopyableGetValue.symbol).run {
                irCall(setKopyableReferenceFunction).apply {
                    dispatchReceiver = irGet(kopyableValueDeclaration)
                    putValueArgument(0, copyCall)
                }
            }

        return setKopyableReferenceCall
    }
}
