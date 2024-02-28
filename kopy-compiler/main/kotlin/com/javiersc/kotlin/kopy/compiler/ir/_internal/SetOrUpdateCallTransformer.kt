package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.filterIrIsInstance
import com.javiersc.kotlin.compiler.extensions.ir.firstIrSimpleFunction
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.compiler.extensions.ir.name
import com.javiersc.kotlin.compiler.extensions.ir.toIrTreeNode
import com.javiersc.kotlin.kopy.KopyFunctionKopy
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
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getArguments
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.indexOrMinusOne
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

    private fun IrExpression.createCopyChainCall(): IrExpression? {
        val expressions: Sequence<IrExpression> = extensionOrDispatchReceiverChain.drop(2)
        if (expressions.any { it.parentClassOrNull?.isData == false }) return null

        val copiedExpressions: Sequence<IrDeclarationReference> =
            expressions.filterIsInstance<IrDeclarationReference>().map {
                val parent: IrDeclarationParent =
                    pluginContext.declarationIrBuilder(it.symbol).parent
                it.deepCopyWithSymbols(parent).also {
                    it.asIrOrNull<IrCall>()?.dispatchReceiver = null
                }
            }

        return null
    }

    private val IrExpression.parentClassOrNull: IrClass?
        get() =
            when (this) {
                is IrCall -> type.classOrNull?.owner
                is IrGetValue -> type.classOrNull?.owner
                else -> null
            }

    private val IrExpression.extensionOrDispatchReceiverChain: Sequence<IrExpression>
        get() {
            val receiversChain: Sequence<IrExpression> =
                generateSequence(this) { it.getExtensionOrDispatchReceiver() }
            return receiversChain
        }

    private fun IrExpression.getExtensionOrDispatchReceiver(): IrExpression? =
        when (this) {
            is IrMemberAccessExpression<*> -> extensionReceiver ?: dispatchReceiver
            else -> null
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
                            val copyChainCall: IrCall =
                                expression.createCopyChainCall(alsoItValueParameterGetValue)
                                    ?: return null
                            val setKopyableReferenceCall: IrCall? =
                                expression.createSetKopyableReferenceCall(
                                    copyChainCall = copyChainCall
                                )
                            if (setKopyableReferenceCall != null) +setKopyableReferenceCall
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

    private fun IrCall.createCopyChainCall(alsoItValueParameterGetValue: IrGetValue): IrCall? {
        val getKopyableRefCall: IrCall = createGetKopyableReferenceCall()
        val getKopyableRefType: IrSimpleType = getKopyableRefCall.type.asIr()

        val chain: List<IrMemberAccessExpression<*>> = dispatchersChain().reversed()
        if (chain.isEmpty()) return null

        val dispatchers: List<IrMemberAccessExpression<*>> = listOf(getKopyableRefCall) + chain

        val calls: List<IrCall> =
            dispatchers
                .zipWithNext { current, next ->
                    val dataClass: IrClassSymbol =
                        if (current.type == getKopyableRefType) getKopyableRefType.classOrFail
                        else current.type.classOrFail

                    val getFun: IrSimpleFunction = next.asIr<IrCall>().symbol.owner
                    val isLast: Boolean = next == dispatchers.last()
                    val argumentValue: IrDeclarationReference =
                        if (isLast) alsoItValueParameterGetValue
                        else next.apply { this.dispatchReceiver = current }

                    val copyCall: IrCall? =
                        current.symbol.createCopyCall(
                            dataClass = dataClass,
                            dispatchReceiver = current,
                            propertyGetFunction = getFun,
                            argumentValue = argumentValue,
                        )
                    copyCall ?: return null
                }
                .reversed()

        val copyChainCall: IrCall =
            calls.reduce { acc, irCall ->
                val argumentIndex: Int =
                    irCall.getArguments().firstNotNullOfOrNull {
                        val index = it.first.indexOrMinusOne
                        if (index != -1) index else null
                    } ?: return null
                irCall.putValueArgument(argumentIndex, acc)
                irCall
            }

        copyChainCall
            .toIrTreeNode()
            .filterIrIsInstance<IrCall>()
            .filter { call ->
                val dispatcherName = call.dispatchReceiver.asIrOrNull<IrCall>()?.name
                dispatcherName in dispatchers.mapNotNull { it.asIrOrNull<IrCall>()?.name }
            }
            .forEach { it.dispatchReceiver = it.dispatchReceiver?.deepCopyWithSymbols() }

        return copyChainCall
    }

    private fun IrCall.createGetKopyableReferenceCall(): IrCall {
        val kopyableGetValue: IrGetValue =
            dispatchReceiver?.asIrOrNull<IrGetValue>() ?: error("No value found")

        val kopyableClass: IrClassSymbol = kopyableGetValue.type.classOrFail
        val getKopyableReferenceFunction: IrSimpleFunctionSymbol =
            kopyableClass.getSimpleFunction("getKopyableReference") ?: error("No function found")

        val getKopyableReferenceCall: IrCall =
            pluginContext.declarationIrBuilder(kopyableGetValue.symbol).run {
                irCall(getKopyableReferenceFunction).apply {
                    dispatchReceiver = kopyableGetValue
                    type = kopyableGetValue.type
                }
            }
        return getKopyableReferenceCall
    }

    private fun IrMemberAccessExpression<*>.dispatchersChain(): List<IrMemberAccessExpression<*>> =
        buildList {
            val extensionReceiver: IrMemberAccessExpression<*> =
                runCatching {
                        extensionReceiver
                            ?.asIrOrNull<IrMemberAccessExpression<*>>()
                            ?.deepCopyWithSymbols()
                    }
                    .getOrNull() ?: return@buildList

            add(extensionReceiver)
            fun extract(expression: IrMemberAccessExpression<*>) {
                val expressionCopy: IrMemberAccessExpression<*> = expression.deepCopyWithSymbols()
                val dispatcher: IrMemberAccessExpression<*>? =
                    expressionCopy.dispatchReceiver.asIrOrNull<IrMemberAccessExpression<*>>()
                if (dispatcher == null) {
                    onEach { it.dispatchReceiver = null }
                    return
                }
                add(dispatcher.deepCopyWithSymbols())
                extract(dispatcher)
            }
            extract(extensionReceiver)
        }

    private fun IrSymbol.createCopyCall(
        dataClass: IrClassSymbol,
        dispatchReceiver: IrExpression,
        propertyGetFunction: IrSimpleFunction,
        argumentValue: IrExpression,
    ): IrCall? {
        val copyCall: IrCall? =
            pluginContext.declarationIrBuilder(this).run {
                val kotlinCopyFunctionSymbol: IrSimpleFunctionSymbol =
                    dataClass.owner.functions
                        .firstOrNull {
                            it.name == "copy".toName() &&
                                !it.hasAnnotation(fqName<KopyFunctionKopy>())
                        }
                        ?.symbol ?: return null
                irCall(kotlinCopyFunctionSymbol).apply {
                    this.dispatchReceiver = dispatchReceiver

                    val copyParamIndex: Int =
                        kotlinCopyFunctionSymbol.owner.valueParameters.indexOfFirst {
                            it.name == propertyGetFunction.correspondingPropertySymbol?.owner?.name
                        }
                    if (copyParamIndex == -1) return@run null
                    putValueArgument(copyParamIndex, argumentValue)
                }
            }
        return copyCall
    }

    private fun IrCall.createSetKopyableReferenceCall(copyChainCall: IrCall): IrCall? {
        val kopyableGetValue: IrGetValue = dispatchReceiver?.asIrOrNull<IrGetValue>() ?: return null

        val kopyableClass: IrClassSymbol = kopyableGetValue.type.classOrFail

        val setKopyableReferenceFunction: IrSimpleFunctionSymbol =
            kopyableClass.getSimpleFunction("setKopyableReference")!!

        val setKopyableReferenceCall: IrCall =
            pluginContext.declarationIrBuilder(kopyableGetValue.symbol).run {
                irCall(setKopyableReferenceFunction).apply {
                    dispatchReceiver = kopyableGetValue.deepCopyWithSymbols()
                    putValueArgument(0, copyChainCall)
                }
            }

        return setKopyableReferenceCall
    }
}
