// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo1 = Foo(number = 7, letter = 'W')
    val foo2 = Foo2(number = 7, letter = 'W')

    val foo21 = foo1 <!INVISIBLE_REFERENCE!>copy<!> {
        <!INVISIBLE_REFERENCE!>number<!> = 42
    }
    val foo22 = foo2 <!INVISIBLE_REFERENCE!>copy<!> {
        <!INVISIBLE_REFERENCE!>number<!> = 42
    }
}

@Kopy
data class Foo(val number: Int, val letter: Char)

@Kopy
data class Foo2 public constructor(val number: Int, val letter: Char)
