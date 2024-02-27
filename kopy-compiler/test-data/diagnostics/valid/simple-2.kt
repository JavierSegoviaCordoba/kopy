// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W', text = "Random")

    val foo21 = foo0 copy {
        number.set(42)
    }

    val isOk = foo21.number == 42

    return if (isOk) "OK" else "Fail: number is ${foo21.number} instead of 42"
}

@Kopy data class Foo(val number: Int, val letter: Char, var text: String)
