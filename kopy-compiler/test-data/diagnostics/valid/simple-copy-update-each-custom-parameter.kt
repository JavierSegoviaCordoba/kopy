// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo0 = Foo(numbers = listOf(7, 2), letter = 'W')

    val foo21 = foo0 copy {
        numbers.updateEach { num -> num + 42 }
    }
}

@Kopy data class Foo(val numbers: List<Int>, val letter: Char)

/* GENERATED_FIR_TAGS: additiveExpression, classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral,
localProperty, primaryConstructor, propertyDeclaration */
