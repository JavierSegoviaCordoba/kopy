// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val quz = Quz("Hello")
    val baz = Baz(quz, 42)
    val bar = Bar(baz, quz)
    val foo = Foo(bar, baz, quz)

    val foo2 = foo.copy {
        bar.quz.text.set("Changed 1")
        quz.text.set("Changed 2")
        bar.baz.quz.text.set("Changed 3")
        bar.quz.text.set("Changed 4")
    }
}

@Kopy data class Quz(val text: String)
@Kopy data class Baz(val quz: Quz, val number: Int)
@Kopy data class Bar(val baz: Baz, val quz: Quz)
@Kopy data class Foo(val bar: Bar, val baz: Baz, val quz: Quz)
