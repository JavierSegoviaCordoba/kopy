// TODO: Remove this file after upgrading to JavierSC Kotlin Compiler Extensions

package com.javiersc.kotlin.kopy.compiler.ir.utils

import com.javiersc.kotlin.compiler.extensions.ir.isContextParameter
import com.javiersc.kotlin.compiler.extensions.ir.isDispatchReceiver
import com.javiersc.kotlin.compiler.extensions.ir.isExtensionReceiver
import com.javiersc.kotlin.compiler.extensions.ir.isRegular
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal inline val IrFunction.dispatchReceiver: IrValueParameter?
    get() = parameters.firstOrNull(IrValueParameter::isDispatchReceiver)

internal inline val IrFunction.contextParameters: List<IrValueParameter>
    get() = parameters.filter(IrValueParameter::isContextParameter)

internal inline val IrFunction.extensionReceiver: IrValueParameter?
    get() = parameters.firstOrNull(IrValueParameter::isExtensionReceiver)

internal inline val IrFunction.regularParameters: List<IrValueParameter>
    get() = parameters.filter(IrValueParameter::isRegular)

internal val IrValueParameter.isDispatchReceiver: Boolean
    get() = kind == IrParameterKind.DispatchReceiver

internal val IrValueParameter.isContextParameter: Boolean
    get() = kind == IrParameterKind.Context

internal val IrValueParameter.isExtensionReceiver: Boolean
    get() = kind == IrParameterKind.ExtensionReceiver

internal val IrValueParameter.isRegular: Boolean
    get() = kind == IrParameterKind.Regular

internal val IrCall.dispatchReceiverArgument: IrExpression?
    get() = argumentsMap().filterValues(IrValueParameter::isDispatchReceiver).keys.firstOrNull()

internal val IrCall.contextParameterArguments: List<IrExpression>
    get() = argumentsMap().filterValues(IrValueParameter::isContextParameter).keys.filterNotNull()

internal val IrCall.extensionReceiverArgument: IrExpression?
    get() = argumentsMap().filterValues(IrValueParameter::isExtensionReceiver).keys.firstOrNull()

internal val IrCall.regularArguments: List<IrExpression>
    get() = argumentsMap().filterValues(IrValueParameter::isRegular).keys.filterNotNull()

internal fun IrCall.argumentsMap(): Map<IrExpression?, IrValueParameter> =
    (arguments zip symbol.owner.parameters).toMap()
