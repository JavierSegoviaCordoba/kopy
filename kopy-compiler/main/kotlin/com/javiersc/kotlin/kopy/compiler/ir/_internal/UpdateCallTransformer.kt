package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.filterIrIsInstance
import com.javiersc.kotlin.compiler.extensions.ir.firstIrSimpleFunction
import com.javiersc.kotlin.compiler.extensions.ir.treeNode
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.SpecialNames

internal class UpdateCallTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        fun originalCall(): IrExpression = super.visitCall(expression)
        if (!expression.isKopyUpdate) return originalCall()

        val letCall: IrCall = createLetCall(expression)

        val copyCall: IrCall = createCopyCall(expression, letCall)

        return copyCall
    }

    private fun createLetCall(updateCall: IrCall): IrCall {
        val declarationParent: IrDeclarationParent =
            updateCall.findDeclarationParent(moduleFragment).asIrOrNull<IrDeclarationParent>()
                ?: return updateCall

        val updateBlockStatements: List<IrStatement> =
            updateCall
                .getValueArgument((updateCall.valueArgumentsCount - 1).takeIf { it >= 0 } ?: 0)
                .asIrOrNull<IrFunctionExpression>()
                ?.function
                ?.body
                ?.statements
                .orEmpty()

        val type: IrType = updateCall.type

        val letFunction: IrSimpleFunction =
            pluginContext.firstIrSimpleFunction("kotlin.let".toCallableId())

        val letBlockFunction: IrSimpleFunction =
            pluginContext.irFactory
                .buildFun {
                    name = SpecialNames.ANONYMOUS
                    visibility = DescriptorVisibilities.LOCAL
                    returnType = type
                    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                }
                .apply {
                    parent = declarationParent
                    addValueParameter {
                        this.name = StandardNames.IMPLICIT_LAMBDA_PARAMETER_NAME
                        this.type = type
                    }
                }

        val letBlockBody: IrBlockBody =
            pluginContext.declarationIrBuilder(letBlockFunction).irBlockBody {
                replaceUpdateBlockItWithLetBlockIt(updateCall, letBlockFunction)
                for (statement in updateBlockStatements) {
                    +processUpdateBlockStatement(statement)
                }
            }
        letBlockFunction.body = letBlockBody

        val letCall: IrCall =
            pluginContext
                .declarationIrBuilder(letFunction.symbol)
                .irCall(letFunction.symbol)
                .apply {
                    this.extensionReceiver = updateCall.extensionReceiver
                    this.dispatchReceiver = null
                    putTypeArgument(index = 0, type = type)
                    putTypeArgument(index = 1, type = type)
                    val kFunction1Type: IrSimpleType =
                        letFunction.valueParameters
                            .first()
                            .type
                            .asIr<IrSimpleType>()
                            .classifier
                            .typeWith(type, type)
                    putValueArgument(
                        index = 0,
                        valueArgument =
                            createIrFunctionExpression(
                                type = kFunction1Type,
                                function = letBlockFunction,
                                origin = IrStatementOrigin.LAMBDA,
                            )
                    )
                    this.type = type
                }

        return letCall
    }

    private fun replaceUpdateBlockItWithLetBlockIt(
        updateCall: IrCall,
        letBlockFunction: IrSimpleFunction,
    ) {
        val updateItValueParameter: IrValueParameter =
            updateCall
                .getValueArgument(0)
                ?.asIrOrNull<IrFunctionExpression>()
                ?.function
                ?.valueParameters
                ?.firstOrNull() ?: return
        val its: List<IrGetValue> =
            updateCall
                .getValueArgument(0)
                ?.treeNode
                ?.asSequence()
                ?.filterIrIsInstance<IrGetValue>()
                ?.filter { it.symbol.owner == updateItValueParameter }
                .orEmpty()
                .toList()

        val letItValueParameter: IrValueParameter = letBlockFunction.valueParameters.first()

        for (it in its) {
            it.symbol = letItValueParameter.symbol
        }
    }

    private fun IrBlockBodyBuilder.processUpdateBlockStatement(
        statement: IrStatement
    ): IrStatement {

        return if (statement is IrReturn) irReturn(statement.value) else statement
    }

    private fun createCopyCall(updateCall: IrCall, letCall: IrCall): IrCall {
        val tempVarGet: IrGetValue =
            updateCall.dispatchReceiver.asIrOrNull<IrGetValue>() ?: return letCall
        val propertySymbol: IrPropertySymbol =
            letCall.extensionReceiver
                .asIrOrNull<IrCall>()
                ?.symbol
                ?.owner
                ?.correspondingPropertySymbol ?: return letCall
        val copyFunction: IrSimpleFunction =
            tempVarGet.type
                .asIrOrNull<IrSimpleType>()
                ?.classifier
                ?.owner
                ?.asIrOrNull<IrClass>()
                ?.functions
                ?.firstOrNull { it.name == "copy".toName() } ?: return letCall

        val copyCall: IrCall =
            pluginContext.declarationIrBuilder(copyFunction).irCall(copyFunction.symbol).apply {
                dispatchReceiver = tempVarGet
                extensionReceiver = null
                for (index in 0 ..< valueArgumentsCount) {
                    val isSameProperty: Boolean =
                        copyFunction.getIsSameProperty(index, propertySymbol)
                    if (!isSameProperty) putValueArgument(index, null)
                    else putValueArgument(index, letCall)
                }
            }

        return copyCall
    }

    private fun IrFunction.getIsSameProperty(
        index: Int,
        propertySymbol: IrPropertySymbol
    ): Boolean =
        valueParameters
            .getOrNull(index)
            ?.asIrOrNull<IrValueParameter>()
            ?.symbol
            ?.owner
            ?.defaultValue
            ?.expression
            ?.asIrOrNull<IrGetField>()
            ?.symbol
            ?.owner
            ?.correspondingPropertySymbol == propertySymbol
}
