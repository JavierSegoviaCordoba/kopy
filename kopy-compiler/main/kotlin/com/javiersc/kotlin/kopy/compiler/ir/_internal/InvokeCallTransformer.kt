package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.SpecialNames

internal class InvokeCallTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        fun originalCall(): IrExpression = super.visitCall(expression)

        if (!expression.isKopyInvoke) return originalCall()
        val runCall: IrFunctionAccessExpression = createRunCall(expression) ?: return originalCall()

        return runCall
    }

    private fun IrSymbol.declarationIrBuilder(): DeclarationIrBuilder =
        pluginContext.declarationIrBuilder(this)

    private fun createRunCall(originalCall: IrCall): IrFunctionAccessExpression? {
        val originalCallParent: IrDeclarationParent =
            originalCall.findDeclarationParent(moduleFragment)?.asIrOrNull<IrDeclarationParent>()
                ?: return null
        val type = originalCall.dispatchReceiver?.type ?: return null

        val runFunction: IrSimpleFunction =
            pluginContext
                .referenceFunctions("kotlin.run".toCallableId())
                .first { it.owner.typeParameters.count() == 2 }
                .owner

        val runBlockFunction: IrSimpleFunction =
            pluginContext.irFactory
                .buildFun {
                    name = SpecialNames.ANONYMOUS
                    visibility = DescriptorVisibilities.LOCAL
                    returnType = type
                    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                }
                .apply {
                    parent = originalCallParent
                    extensionReceiverParameter =
                        buildValueParameter(this) {
                            this.name = "${'$'}this${'$'}run".toName()
                            this.type = type
                        }
                }

        val originalStatements =
            originalCall
                .getValueArgument(0)
                .asIrOrNull<IrFunctionExpression>()
                ?.function
                ?.body
                ?.statements ?: return null

        val runBlockFunctionBody: IrBlockBody =
            runBlockFunction.symbol.declarationIrBuilder().irBlockBody {
                val thisRunReceiver: IrGetValue =
                    runBlockFunction.extensionReceiverParameter?.let { valueParameter ->
                        runBlockFunction.symbol.declarationIrBuilder().irGet(valueParameter)
                    } ?: return@irBlockBody

                val tempVar: IrVariable = irTemporary(value = thisRunReceiver, isMutable = true)
                for (statement in originalStatements) {
                    +statement.processStatement(originalCall, runBlockFunction, tempVar)
                }
                +irReturn(runBlockFunction.symbol.declarationIrBuilder().irGet(tempVar))
            }

        runBlockFunction.body = runBlockFunctionBody

        val runCall: IrFunctionAccessExpression =
            runFunction.symbol.declarationIrBuilder().irCall(runFunction).apply {
                extensionReceiver = originalCall.dispatchReceiver
                putTypeArgument(index = 0, type = type)
                putTypeArgument(index = 1, type = type)
                val kFunction1Type: IrSimpleType =
                    runFunction.valueParameters
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
                            function = runBlockFunction,
                            origin = IrStatementOrigin.LAMBDA,
                        )
                )
                this.type = type
            }

        return runCall
    }

    private fun IrStatement.processStatement(
        originalCall: IrCall,
        runBlockFunction: IrSimpleFunction,
        tempVar: IrVariable,
    ): IrStatement {
        val operatorCall: IrFunctionAccessExpression =
            asIrOrNull<IrTypeOperatorCall>()?.argument?.asIrOrNull() ?: return this

        val original = originalCall.dispatchReceiver.asIrOrNull<IrGetValue>() ?: return this
        val originalType = original.type
        val originalName = original.symbol.descriptor.name

        val operator = operatorCall.dispatchReceiver.asIrOrNull<IrGetValue>() ?: return this
        val operatorType = operator.type
        val operatorName = operator.symbol.descriptor.name

        val originalThisName = "${'$'}this${'$'}$originalName"
        val originalThisCopyName = "${'$'}this${'$'}copy"
        val hasSameReceiver =
            originalType == operatorType &&
                (originalThisName == "$operatorName" || originalThisCopyName == "$operatorName")

        if (!hasSameReceiver) return this

        operatorCall.apply {
            val receiver: () -> IrGetValue = {
                runBlockFunction.symbol.declarationIrBuilder().irGet(tempVar)
            }
            dispatchReceiver = receiver()
            extensionReceiver.asIrOrNull<IrFunctionAccessExpression>()?.dispatchReceiver =
                receiver()
        }
        val tempVarSet: IrSetValue =
            runBlockFunction.symbol.declarationIrBuilder().irSet(tempVar, operatorCall)

        return tempVarSet
    }
}
