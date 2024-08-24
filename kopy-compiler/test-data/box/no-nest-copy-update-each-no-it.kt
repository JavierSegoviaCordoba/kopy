// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo0 = Foo(numbers = listOf(7, 3), letters = listOf('W'))

    val foo21 = foo0 copy {
        numbers.updateEach { 42 }
    }

    val isOk = foo21.numbers.all { it == 42 }

    return if (isOk) "OK" else "Fail: numbers are ${foo21.numbers} instead of all being 42"
}

@Kopy
data class Foo(val numbers: List<Int>, val letters: List<Char>)
