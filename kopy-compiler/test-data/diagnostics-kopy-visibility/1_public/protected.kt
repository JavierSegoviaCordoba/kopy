// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val foo1 = <!INVISIBLE_REFERENCE!>Foo<!>(number = 7, letter = 'W')

    val foo21 = foo1 <!INVISIBLE_REFERENCE!>copy<!> {
        <!INVISIBLE_REFERENCE!>number<!> = 42
    }
}

@Kopy
data class Foo <!DATA_CLASS_COPY_VISIBILITY_WILL_BE_CHANGED_WARNING!>protected<!> constructor(val number: Int, val letter: Char)
