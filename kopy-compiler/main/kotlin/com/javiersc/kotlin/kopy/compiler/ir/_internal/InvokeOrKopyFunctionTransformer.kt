@file:Suppress("DEPRECATION_ERROR")

package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.ir.asIr
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.firstIrClass
import com.javiersc.kotlin.kopy.compiler.ir._internal.utils.isKopyInvokeOrKopy
import com.javiersc.kotlin.kopy.runtime.KopyableScope
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor

internal class InvokeOrKopyFunctionTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    private val kopyableScopeClass: IrClass by lazy {
        pluginContext.firstIrClass(classId<KopyableScope<*>>())
    }

    private val kFunction2InvokeFunction: IrSimpleFunction by lazy {
        pluginContext.irBuiltIns.functionN(2).getSimpleFunction("invoke")!!.owner
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFunction(): IrStatement = super.visitSimpleFunction(declaration)

        if (!declaration.isKopyInvokeOrKopy) return originalFunction()

        val function: IrSimpleFunction =
            declaration.apply {
                body =
                    pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                        val scopeVariable: IrVariable =
                            irTemporary(value = createKopyableScopeConstructorCall(declaration))
                        +createKopyValueArgumentCall(declaration, scopeVariable)
                        val scopeVariableGetCall: IrCall =
                            irCall(scopeVariable.getKopyableReferenceFunction).apply {
                                dispatchReceiver = irGet(scopeVariable)
                            }
                        +irReturn(scopeVariableGetCall)
                    }
            }
        return function
    }

    private fun createKopyableScopeConstructorCall(
        invokeOrKopyFunction: IrSimpleFunction,
    ): IrConstructorCall {
        val thisReceiverGet: IrGetValue =
            pluginContext
                .declarationIrBuilder(invokeOrKopyFunction.parentAsClass.symbol)
                .irGet(invokeOrKopyFunction.dispatchReceiverParameter!!)
                .apply { symbol.owner.parent = invokeOrKopyFunction }
        val typeParameterType: IrType = invokeOrKopyFunction.returnType
        val kopyableScopeConstructorSymbol: IrConstructorSymbol =
            kopyableScopeClass.primaryConstructor!!.symbol
        val constructorCall: IrConstructorCall =
            pluginContext.declarationIrBuilder(kopyableScopeClass.symbol).run {
                irCallConstructor(kopyableScopeConstructorSymbol, listOf(typeParameterType)).apply {
                    type = kopyableScopeClass.typeWith(typeParameterType)
                    putValueArgument(0, thisReceiverGet)
                }
            }
        return constructorCall
    }

    private fun createKopyValueArgumentCall(
        invokeOrKopyFunction: IrSimpleFunction,
        scopeVariable: IrVariable
    ): IrCall {
        val kopyArgument: IrValueParameter = invokeOrKopyFunction.valueParameters.first()
        val kopyValueArgumentCall: IrCall =
            pluginContext.declarationIrBuilder(invokeOrKopyFunction.symbol).run {
                irCall(callee = kFunction2InvokeFunction.symbol)
                    .apply {
                        dispatchReceiver = irGet(kopyArgument)
                        putValueArgument(0, irGet(scopeVariable))
                        val scopeVariableGetCall: IrCall =
                            irCall(scopeVariable.getKopyableReferenceFunction).apply {
                                dispatchReceiver = irGet(scopeVariable)
                            }
                        putValueArgument(1, scopeVariableGetCall)
                    }
                    .asIr()
            }
        return kopyValueArgumentCall
    }

    private val IrVariable.getKopyableReferenceFunction: IrSimpleFunctionSymbol
        get() = type.classOrFail.getSimpleFunction("getKopyableReference")!!
}
