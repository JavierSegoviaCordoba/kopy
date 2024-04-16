// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val bar0 = Bar(number = 7, letter = 'W')
    val foo0 = Foo(bar= bar0)

    val foo21 = foo0 copy {
        bar.number = 42
    }
}

@Kopy data class Foo(val bar: Bar)
@Kopy data class Bar(val number: Int, val letter: Char)
