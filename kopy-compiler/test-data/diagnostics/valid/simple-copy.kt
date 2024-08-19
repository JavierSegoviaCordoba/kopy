// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo0 = Foo(number = 7, letter = 'W')

    val foo21 = foo0 copy {
        number = 42
    }
}

@Kopy data class Foo(val number: Int, val letter: Char)
