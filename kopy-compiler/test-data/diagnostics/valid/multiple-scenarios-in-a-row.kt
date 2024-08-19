// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val qux0 = Qux(number = 7, items = listOf(7, 11))
    val baz0 = Baz(qux = qux0, text = "Random", items = setOf(7, 11))
    val bar0 = Bar(baz = baz0, isValid = true, items = mutableListOf(7, 11))
    val foo0 = Foo(bar = bar0, letter = 'W', items = sequenceOf(7, 11))

    val foo10 = foo0 {}
    val foo11 = foo0 copy {}

    val foo20 = foo0 { bar.baz.qux.number.set(10) }
    val foo21 = foo0 copy { bar.baz.qux.number.set(10) }

    val foo30 = foo0 { bar.baz.qux.number.update { it + 10 } }
    val foo31 = foo0 copy { bar.baz.qux.number.update { it + 10 } }

    val foo40 = foo0 { bar.baz.text.set("Random 2") }
    val foo41 = foo0 copy { bar.baz.text.set("Random 2") }

    val foo50 = foo0 { bar.baz.text.update { it + " 2" } }
    val foo51 = foo0 copy { bar.baz.text.update { it + " 2" } }

    val foo60 = foo0 {
        bar.baz.qux.number.set(10)
        bar.baz.qux.items.updateEach { it + 42 }
        bar.baz.text.update { "$it Random 2" }
        bar.baz.items.updateEach { it + 42 }
        bar.isValid.set(false)
        bar.items.updateEach { it + 42 }
    }

    val foo61 = foo0 {
        bar.baz.qux.number.update { it + 10 }
        bar.baz.qux.items.updateEach { it + 420 }
        bar.baz.text.set("Random 2")
        bar.baz.items.updateEach { it + 4200 }
        bar.isValid.update { !it }
        bar.items.updateEach { it + 4200 }
    }
}

@Kopy data class Qux(val number: Int, val items: List<Int>)
@Kopy data class Baz(val qux: Qux, val text: String, val items: Set<Int>)
@Kopy data class Bar(val baz: Baz, val isValid: Boolean, val items: MutableList<Int>)
@Kopy data class Foo(val bar: Bar, val letter: Char, val items: Sequence<Int>)
