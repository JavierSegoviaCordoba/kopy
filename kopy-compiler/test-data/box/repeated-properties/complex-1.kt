// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val quz0 = Quz("Hello")
    val baz0 = Baz(quz0, 42)
    val bar0 = Bar(baz0, quz0)
    val foo0 = Foo(bar0, baz0, quz0)

    val foo2 = foo0.copy {
        bar.quz.text.set("Changed 1")
        quz.text.set("Changed 2")
        bar.baz.quz.text.set("Changed 3")
    }
    val isOneOk = foo2.bar.quz.text == "Changed 1"
    val isTwoOk = foo2.quz.text == "Changed 2"
    val isThreeOk = foo2.bar.baz.quz.text == "Changed 3"
    val isOk = isOneOk && isTwoOk && isThreeOk

    return if (isOk) "OK" else "Fail:\n$foo2"
}

@Kopy data class Quz(val text: String)
@Kopy data class Baz(val quz: Quz, val number: Int)
@Kopy data class Bar(val baz: Baz, val quz: Quz)
@Kopy data class Foo(val bar: Bar, val baz: Baz, val quz: Quz)
