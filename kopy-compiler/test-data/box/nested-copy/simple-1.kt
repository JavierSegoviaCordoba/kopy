// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W')

    val foo21 = foo0 copy {
        val foo11 = foo0 copy {
            number.set(42)
        }
        number.set(foo11.number)
    }

    val isOk = foo21.number == 42 && foo21.letter == 'W'
    return if (isOk) "OK" else "Fail: $foo21"
}

@Kopy data class Foo(val number: Int, val letter: Char)
