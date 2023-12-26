// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val qux0 = Qux(number = 7)
    val baz0 = Baz(qux = qux0, text = "Random")
    val bar0 = Bar(baz = baz0, isValid = true)
    val foo0 = Foo(bar = bar0, letter = 'W')

    val foo61 = foo0 {
        bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.update { it + 10 }
    }
}

@Kopy data class Qux(val number: Int)
@Kopy class Baz(val qux: Qux, val text: String)
@Kopy data class Bar(val baz: Baz, val isValid: Boolean)
@Kopy data class Foo(val bar: Bar, val letter: Char)
