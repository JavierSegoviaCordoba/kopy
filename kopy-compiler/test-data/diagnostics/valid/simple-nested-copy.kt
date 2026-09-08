// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo0 = Foo(number = 7, letter = 'W')

    val foo21 = foo0 copy {
        val foo11 = foo0 copy {
            number.set(42)
        }
        number.set(foo11.number)
    }
}

@Kopy data class Foo(val number: Int, val letter: Char)

/* GENERATED_FIR_TAGS: classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral, localProperty,
primaryConstructor, propertyDeclaration */
