@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.ir.DeclarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.createIrFunctionExpression
import com.javiersc.kotlin.compiler.extensions.ir.dispatchReceiverArgument
import com.javiersc.kotlin.compiler.extensions.ir.extensionReceiverArgument
import com.javiersc.kotlin.compiler.extensions.ir.filterIrIsInstance
import com.javiersc.kotlin.compiler.extensions.ir.firstIrSimpleFunction
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.compiler.extensions.ir.name
import com.javiersc.kotlin.compiler.extensions.ir.regularArguments
import com.javiersc.kotlin.compiler.extensions.ir.regularParameters
import com.javiersc.kotlin.compiler.extensions.ir.toIrTreeNode
import com.javiersc.kotlin.kopy.compiler.alsoCallableId
import com.javiersc.kotlin.kopy.compiler.copyName
import com.javiersc.kotlin.kopy.compiler.ir.utils.findDeclarationParent
import com.javiersc.kotlin.kopy.compiler.ir.utils.insertExtensionReceiver
import com.javiersc.kotlin.kopy.compiler.ir.utils.insertFirstRegularParameter
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopySet
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopySetOrUpdate
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyUpdate
import com.javiersc.kotlin.kopy.compiler.kopyFunctionCopyFqName
import com.javiersc.kotlin.kopy.compiler.loadName
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
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
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.indexOrMinusOne
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.SpecialNames

internal class IrSetOrUpdateCallTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitCall(expression: IrCall): IrExpression {
        fun originalCall(): IrExpression = super.visitCall(expression)
        if (!expression.isKopySetOrUpdate) return originalCall()

        val transformedCall: IrCall =
            when {
                expression.isKopySet -> expression.transformSetValue()
                expression.isKopyUpdate -> expression.transformUpdateReturn()
                else -> return originalCall()
            }
        return transformedCall
    }

    private fun IrCall.transformSetValue(): IrCall = also {
        it.transform(SetValueTransformer(it), null)
    }

    private fun IrCall.transformUpdateReturn(): IrCall = also {
        it.transformStatement(UpdateReturnTransformer(it))
    }

    private inner class SetValueTransformer(private val expressionCall: IrCall) :
        IrElementTransformerVoid() {

        override fun visitExpression(expression: IrExpression): IrExpression {
            fun original(): IrExpression = super.visitExpression(expression)
            if (expression !in expressionCall.regularArguments) return original()
            return expression.createAlsoCallForSameParentAs(expressionCall) ?: original()
        }
    }

    private inner class UpdateReturnTransformer(private val expressionCall: IrCall) :
        IrElementTransformerVoid() {

        override fun visitReturn(expression: IrReturn): IrExpression {
            fun original(): IrExpression = super.visitReturn(expression)
            val parent: IrDeclarationParent =
                expression.findDeclarationParent() ?: return original()
            val updateLambda: IrSimpleFunction =
                expressionCall.regularArguments
                    .firstOrNull()
                    ?.asIrOrNull<IrFunctionExpression>()
                    ?.function ?: return original()
            if (parent != updateLambda) return original()
            val alsoCall: IrCall =
                context(pluginContext) {
                    createAlsoCall(expressionCall, parent) ?: return original()
                }
            val value: IrExpression = expression.value
            return expression.also { irReturn ->
                irReturn.value = alsoCall.withExtensionReceiver(value)
            }
        }
    }

    private fun IrExpression.createAlsoCallForSameParentAs(expressionCall: IrCall): IrCall? {
        val parent: IrDeclarationParent = findDeclarationParent() ?: return null
        if (parent != expressionCall.findDeclarationParent()) return null
        return context(pluginContext) { createAlsoCall(expressionCall, parent) }
            ?.withExtensionReceiver(this)
    }

    private fun IrCall.withExtensionReceiver(expression: IrExpression): IrCall = also {
        it.insertExtensionReceiver(expression)
    }

    context(context: IrPluginContext)
    private fun createAlsoCall(
        expression: IrCall,
        expressionParent: IrDeclarationParent,
    ): IrCall? {
        val setOrUpdateType: IrType = expression.extensionReceiverArgument?.type ?: return null

        val alsoBlockFunction: IrSimpleFunction =
            createAlsoBlockFunction(
                expressionParent = expressionParent,
                setOrUpdateType = setOrUpdateType,
                expression = expression,
            ) ?: return null

        val alsoFunction: IrSimpleFunction = firstIrSimpleFunction(alsoCallableId)
        val alsoBlockFunctionExpression: IrFunctionExpression =
            createAlsoBlockFunctionExpression(alsoBlockFunction, setOrUpdateType)
        val alsoCall: IrCall =
            createAlsoCallWithBlock(
                alsoFunction = alsoFunction,
                expressionParent = expressionParent,
                setOrUpdateType = setOrUpdateType,
                alsoBlockFunctionExpression = alsoBlockFunctionExpression,
            )

        return alsoCall
    }

    context(context: IrPluginContext)
    private fun createAlsoBlockFunctionExpression(
        alsoBlockFunction: IrSimpleFunction,
        setOrUpdateType: IrType,
    ): IrFunctionExpression =
        createIrFunctionExpression(
            type = context.irBuiltIns.run { functionN(1).typeWith(setOrUpdateType, unitType) },
            function = alsoBlockFunction,
            origin = IrStatementOrigin.LAMBDA,
        )

    context(context: IrPluginContext)
    private fun createAlsoCallWithBlock(
        alsoFunction: IrSimpleFunction,
        expressionParent: IrDeclarationParent,
        setOrUpdateType: IrType,
        alsoBlockFunctionExpression: IrFunctionExpression,
    ): IrCall =
        DeclarationIrBuilder(alsoFunction.symbol).run {
            irCall(alsoFunction.symbol).also {
                it.patchDeclarationParents(expressionParent)
                it.type = setOrUpdateType
                it.typeArguments[0] = setOrUpdateType
                it.insertFirstRegularParameter(alsoBlockFunctionExpression)
            }
        }

    private fun IrElement.findDeclarationParent(): IrDeclarationParent? =
        findDeclarationParent(moduleFragment)?.asIrOrNull<IrDeclarationParent>()

    context(context: IrPluginContext)
    private fun createAlsoBlockFunction(
        expressionParent: IrDeclarationParent,
        setOrUpdateType: IrType,
        expression: IrCall,
    ): IrSimpleFunction? {
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
                    body = createAlsoBlockBody(expression) ?: return null
                }
        return alsoBlockFunction
    }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createAlsoBlockBody(expression: IrCall): IrBlockBody? =
        DeclarationIrBuilder(symbol).irBlockBody {
            val alsoItValueParameterGetValue: IrGetValue = createGetValueForAlsoIt()
            val copyChainCall: IrCall =
                expression.createCopyChainCall(alsoItValueParameterGetValue) ?: return null
            val storeCopyChainCall: IrCall =
                expression.createAtomicGetterStoreSetFunctionCall(copyChainCall)
            +storeCopyChainCall
        }

    context(context: IrPluginContext)
    private fun IrSimpleFunction.createGetValueForAlsoIt(): IrGetValue {
        val itValueParameter: IrValueParameter = regularParameters.first()
        return DeclarationIrBuilder(symbol).irGet(itValueParameter)
    }

    context(context: IrPluginContext)
    private fun IrCall.createCopyChainCall(alsoItValueParameterGetValue: IrGetValue): IrCall? {
        val atomicGetterGetFunctionCall: IrCall = createAtomicGetterGetFunctionCall()
        val atomicRefType: IrSimpleType = atomicGetterGetFunctionCall.type.asIr()
        val dispatchers: List<IrMemberAccessExpression<*>> =
            createDispatchersForCopyChain(atomicGetterGetFunctionCall) ?: return null

        val calls: List<IrCall> =
            dispatchers.createCopyCalls(atomicRefType, alsoItValueParameterGetValue) ?: return null

        val copyChainCall: IrCall = calls.createNestedCopyCall() ?: return null
        copyChainCall.deepCopyNestedDispatchReceivers(dispatchers)

        return copyChainCall
    }

    private fun IrCall.createDispatchersForCopyChain(
        atomicGetterGetFunctionCall: IrCall
    ): List<IrMemberAccessExpression<*>>? {
        val chain: List<IrMemberAccessExpression<*>> = dispatchersChain().reversed()
        if (chain.isEmpty()) return null
        return listOf(atomicGetterGetFunctionCall) + chain
    }

    context(context: IrPluginContext)
    private fun List<IrMemberAccessExpression<*>>.createCopyCalls(
        atomicRefType: IrSimpleType,
        alsoItValueParameterGetValue: IrGetValue,
    ): List<IrCall>? =
        zipWithNext { current, next ->
                val dataClass: IrClassSymbol = current.copyReceiverClass(atomicRefType)
                val propertyGetFunction: IrSimpleFunction = next.asIr<IrCall>().symbol.owner
                val argumentValue: IrDeclarationReference =
                    next.copyArgumentValue(
                        current = current,
                        isLast = next == last(),
                        alsoItValueParameterGetValue = alsoItValueParameterGetValue,
                    ) ?: return null

                current.symbol.createCopyCall(
                    dataClass = dataClass,
                    dispatchReceiver = current,
                    propertyGetFunction = propertyGetFunction,
                    argumentValue = argumentValue,
                ) ?: return null
            }
            .reversed()

    private fun IrMemberAccessExpression<*>.copyReceiverClass(
        atomicRefType: IrSimpleType
    ): IrClassSymbol = if (type == atomicRefType) atomicRefType.classOrFail else type.classOrFail

    private fun IrMemberAccessExpression<*>.copyArgumentValue(
        current: IrMemberAccessExpression<*>,
        isLast: Boolean,
        alsoItValueParameterGetValue: IrGetValue,
    ): IrDeclarationReference? =
        if (isLast) alsoItValueParameterGetValue
        else runCatching { apply { dispatchReceiver = current } }.getOrNull()

    private fun List<IrCall>.createNestedCopyCall(): IrCall? = reduceOrNull { acc, irCall ->
        val argumentIndex: Int = irCall.firstCopyArgumentIndex() ?: return null
        irCall.arguments[argumentIndex] = acc
        irCall
    }

    private fun IrCall.firstCopyArgumentIndex(): Int? =
        getArgumentsWithIr().firstNotNullOfOrNull { (valueParameter, _) ->
            val index: Int = valueParameter.descriptor.indexOrMinusOne
            if (index != -1) index + 1 else null
        }

    private fun IrCall.deepCopyNestedDispatchReceivers(
        dispatchers: List<IrMemberAccessExpression<*>>
    ) {
        this.toIrTreeNode()
            .asSequence()
            .filterIrIsInstance<IrCall>()
            .filter { call ->
                val dispatcherName = call.dispatchReceiver.asIrOrNull<IrCall>()?.name
                dispatcherName in dispatchers.mapNotNull { it.asIrOrNull<IrCall>()?.name }
            }
            .forEach { it.dispatchReceiver = it.dispatchReceiver?.deepCopyWithSymbols() }
    }

    private fun IrMemberAccessExpression<IrFunctionSymbol>.dispatchersChain():
        List<IrMemberAccessExpression<*>> = buildList {
        val extensionReceiver: IrMemberAccessExpression<*> =
            runCatching {
                    extensionReceiverArgument
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

    context(context: IrPluginContext)
    private fun IrSymbol.createCopyCall(
        dataClass: IrClassSymbol,
        dispatchReceiver: IrExpression,
        propertyGetFunction: IrSimpleFunction,
        argumentValue: IrExpression,
    ): IrCall? {
        val copyCall: IrCall? =
            DeclarationIrBuilder(this).run {
                val kotlinCopyFunctionSymbol: IrSimpleFunctionSymbol =
                    dataClass.owner.functions
                        .firstOrNull {
                            it.name == copyName && !it.hasAnnotation(kopyFunctionCopyFqName)
                        }
                        ?.symbol ?: return null
                irCall(kotlinCopyFunctionSymbol).apply {
                    this.dispatchReceiver = dispatchReceiver

                    val copyParamIndex: Int =
                        kotlinCopyFunctionSymbol.owner.parameters.indexOfFirst {
                            it.name == propertyGetFunction.correspondingPropertySymbol?.owner?.name
                        }
                    if (copyParamIndex == -1) return@run null
                    arguments[copyParamIndex] = argumentValue
                }
            }
        return copyCall
    }

    context(context: IrPluginContext)
    private fun IrCall.createAtomicGetterGetFunctionCall(): IrCall {
        val dispatchIrGetValue: IrGetValue = findDispatchIrGetValue()

        val dispatchClass: IrClassSymbol = dispatchIrGetValue.type.classOrFail

        val atomicGetterFunction: IrSimpleFunctionSymbol =
            dispatchClass.getPropertyGetter("_atomic") ?: error("No function found")

        val getAtomicGetterFunctionCall: IrCall =
            DeclarationIrBuilder(dispatchIrGetValue.symbol).run {
                irCall(atomicGetterFunction).apply {
                    dispatchReceiver = dispatchIrGetValue
                    type = atomicGetterFunction.owner.returnType
                    origin = IrStatementOrigin.GET_PROPERTY
                }
            }

        val atomicReferenceLoadFunction: IrSimpleFunctionSymbol =
            atomicGetterFunction.owner.returnType.classOrFail.getSimpleFunction("$loadName")
                ?: error("No function found")

        val atomicGetterGetFunctionCall: IrCall =
            DeclarationIrBuilder(dispatchIrGetValue.symbol).run {
                irCall(atomicReferenceLoadFunction).apply {
                    dispatchReceiver = getAtomicGetterFunctionCall
                    type = dispatchIrGetValue.type
                    origin = IrStatementOrigin.GET_PROPERTY
                }
            }

        return atomicGetterGetFunctionCall
    }

    context(context: IrPluginContext)
    private fun IrCall.createAtomicGetterStoreSetFunctionCall(copyChainCall: IrCall): IrCall {
        val dispatchIrGetValue: IrGetValue = findDispatchIrGetValue()

        val dispatchClass: IrClassSymbol = dispatchIrGetValue.type.classOrFail

        val atomicGetterFunction: IrSimpleFunctionSymbol =
            dispatchClass.getPropertyGetter("_atomic") ?: error("No function found")

        val atomicGetterFunctionCall: IrCall =
            DeclarationIrBuilder(dispatchIrGetValue.symbol).run {
                irCall(atomicGetterFunction).apply {
                    dispatchReceiver = dispatchIrGetValue
                    type = atomicGetterFunction.owner.returnType
                    origin = IrStatementOrigin.GET_PROPERTY
                }
            }

        val atomicGetterStoreFunction: IrSimpleFunctionSymbol =
            atomicGetterFunction.owner.returnType.classOrFail.getSimpleFunction("store")
                ?: error("No function found")

        val atomicGetterStoreFunctionCall: IrCall =
            DeclarationIrBuilder(dispatchIrGetValue.symbol).run {
                irCall(atomicGetterStoreFunction).apply {
                    dispatchReceiver = atomicGetterFunctionCall
                    type = atomicGetterStoreFunction.owner.returnType
                    arguments[1] = copyChainCall
                }
            }

        return atomicGetterStoreFunctionCall
    }

    private fun IrCall.findDispatchIrGetValue(): IrGetValue =
        sequenceOf(dispatchReceiverArgument, dispatchReceiver, extensionReceiverArgument)
            .firstNotNullOfOrNull { receiver ->
                generateSequence(receiver) { current ->
                        current.asIrOrNull<IrMemberAccessExpression<*>>()?.dispatchReceiver
                    }
                    .firstNotNullOfOrNull { it.asIrOrNull<IrGetValue>() }
            }
            ?.deepCopyWithSymbols() ?: error("No value found")
}
