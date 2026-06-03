package com.javiersc.kotlin.kopy.compiler.ir.utils

import com.javiersc.kotlin.compiler.extensions.ir.dispatchReceiver
import com.javiersc.kotlin.compiler.extensions.ir.extensionReceiver
import com.javiersc.kotlin.compiler.extensions.ir.isRegular
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal fun IrCall.insertExtensionReceiver(expression: IrExpression?): IrCall {
    val call: IrCall = this
    val parameter: IrValueParameter = call.symbol.owner.extensionReceiver ?: return call
    call.arguments[parameter.indexInParameters] = expression
    return call
}

internal fun IrCall.insertDispatchReceiver(expression: IrExpression?): IrCall {
    val call: IrCall = this
    val parameter: IrValueParameter = call.symbol.owner.dispatchReceiver ?: return call
    call.arguments[parameter.indexInParameters] = expression
    return call
}

internal fun IrCall.insertFirstRegularParameter(expression: IrExpression?): IrCall {
    val call: IrCall = this
    val parameter: IrValueParameter =
        call.symbol.owner.parameters.firstOrNull(IrValueParameter::isRegular) ?: return call
    call.arguments[parameter.indexInParameters] = expression
    return call
}
