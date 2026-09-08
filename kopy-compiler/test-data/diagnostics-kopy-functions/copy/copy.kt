// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

@Kopy
data class Foo(val number: Int, val letter: Char)

/* GENERATED_FIR_TAGS: classDeclaration, data, primaryConstructor, propertyDeclaration */
