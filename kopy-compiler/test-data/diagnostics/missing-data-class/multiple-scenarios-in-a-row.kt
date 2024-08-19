// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val qux0 = Qux(number = 7)
    val baz0 = Baz(qux = qux0, text = "Random")
    val bar0 = Bar(baz = baz0, isValid = true)
    val foo0 = Foo(bar = bar0, letter = 'W')

    val foo10 = foo0 {}
    val foo11 = foo0 copy {}

    val foo20 = foo0 { bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.set(10) }
    val foo21 = foo0 copy { bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.set(10) }

    val foo30 = foo0 { bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.update { it + 10 } }
    val foo31 = foo0 copy { bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.update { it + 10 } }

    val foo40 = foo0 { bar.<!MISSING_DATA_CLASS!>baz<!>.text.set("Random 2") }
    val foo41 = foo0 copy { bar.<!MISSING_DATA_CLASS!>baz<!>.text.set("Random 2") }

    val foo50 = foo0 { bar.<!MISSING_DATA_CLASS!>baz<!>.text.update { it + " 2" } }
    val foo51 = foo0 copy { bar.<!MISSING_DATA_CLASS!>baz<!>.text.update { it + " 2" } }

    val foo60 = foo0 {
        bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.set(10)
        bar.<!MISSING_DATA_CLASS!>baz<!>.text.update { "$it Random 2" }
        bar.isValid.set(false)
    }

    val foo61 = foo0 {
        bar.<!MISSING_DATA_CLASS!>baz<!>.qux.number.update {it + 10 }
        bar.<!MISSING_DATA_CLASS!>baz<!>.text.set("Random 2")
        bar.isValid.update { !it }
    }
}

@Kopy data class Qux(val number: Int)
<!NON_DATA_CLASS_KOPY_ANNOTATED!>@Kopy<!> class Baz(val qux: Qux, val text: String)
@Kopy data class Bar(val baz: Baz, val isValid: Boolean)
@Kopy data class Foo(val bar: Bar, val letter: Char)
