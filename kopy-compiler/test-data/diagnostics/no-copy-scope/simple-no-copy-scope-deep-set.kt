// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val qux0 = Qux(number = 7)
    val baz0 = Baz(qux = qux0, text = "Random")
    val bar0 = Bar(baz = baz0, isValid = true)
    val foo0 = Foo(bar = bar0, letter = 'W')

    val foo21 = foo0.apply { bar.baz.qux.number.<!NO_COPY_SCOPE!>set<!>(42) }
}

data class Qux(val number: Int)
data class Baz(val qux: Qux, val text: String)
data class Bar(val baz: Baz, val isValid: Boolean)
@Kopy data class Foo(val bar: Bar, val letter: Char)
