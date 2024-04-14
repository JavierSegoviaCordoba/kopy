// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo0 = Foo(numbers = listOf(7, 2), letter = 'W')

    val foo21 = foo0 copy {
        numbers.updateEach { num -> num + 42 }
    }
}

@Kopy data class Foo(val numbers: List<Int>, val letter: Char)
