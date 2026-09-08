// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo0 = Foo(bar = Bar(number = 7))

    val foo21 = foo0 copy {
        bar.number = 42
    }
}

@Kopy data class Foo(val bar: Bar)
@Kopy data class Bar(val number: Int)

/* GENERATED_FIR_TAGS: classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral, localProperty,
primaryConstructor, propertyDeclaration */
