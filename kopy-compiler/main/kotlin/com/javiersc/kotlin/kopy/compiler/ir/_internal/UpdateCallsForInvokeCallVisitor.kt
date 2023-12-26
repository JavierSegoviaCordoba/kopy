package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

internal class UpdateCallsForInvokeCallVisitor(
    private val symbol: IrValueParameterSymbol,
) : IrElementVisitorVoid {

    private val _calls: MutableList<IrCall> = mutableListOf()
    val calls: List<IrCall> = _calls

    override fun visitCall(expression: IrCall) {
        if (!expression.isKopyUpdate) return super.visitCall(expression)
        val dispatchReceiverSymbol = expression.dispatchReceiver?.asIrOrNull<IrGetValue>()?.symbol
        if (symbol == dispatchReceiverSymbol) _calls.add(expression)
    }
}
