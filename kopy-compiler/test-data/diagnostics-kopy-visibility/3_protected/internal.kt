// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo1 = Foo(number = 7, letter = 'W')

    val foo21 = foo1 <!INVISIBLE_REFERENCE!>copy<!> {
        <!INVISIBLE_REFERENCE!>number<!> = 42
    }
}

@Kopy
data class Foo <!DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING!>internal<!> constructor(val number: Int, val letter: Char)

/* GENERATED_FIR_TAGS: classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral, localProperty,
primaryConstructor, propertyDeclaration */
