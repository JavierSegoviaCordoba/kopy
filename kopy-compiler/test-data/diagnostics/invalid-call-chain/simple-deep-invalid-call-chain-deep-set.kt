// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val qux0 = Qux(number = 7)
    val baz0 = Baz(qux = qux0, text = "Random")
    val bar0 = Bar(baz = baz0, isValid = true)
    val foo0 = Foo(bar = bar0, letter = 'W')

    val foo10 = foo0 { <!INVALID_CALL_CHAIN!>bar0<!>.baz.qux.number.set(42) }
    val foo11 = foo0 copy { <!INVALID_CALL_CHAIN!>bar0<!>.baz.qux.number.set(42) }

    val foo20 = foo0 { bar.baz.<!INVALID_CALL_CHAIN!>apply<!> {}.qux.number.set(42) }
    val foo21 = foo0 copy { bar.baz.<!INVALID_CALL_CHAIN!>apply<!> {}.qux.number.set(42) }
}

@Kopy data class Qux(val number: Int)
@Kopy data class Baz(val qux: Qux, val text: String)
@Kopy data class Bar(val baz: Baz, val isValid: Boolean)
@Kopy data class Foo(val bar: Bar, val letter: Char)
