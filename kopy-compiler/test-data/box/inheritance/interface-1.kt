// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo = Foo(bar = Qux())
    val foo2 = foo.copy { bar = Quux() }
    val foo3 = foo.copy { bar = Corge() }

    val isOk: Boolean = foo.bar is Qux && foo2.bar is Quux && foo3.bar is Corge

    return if (isOk) "OK" else "Error"
}

@Kopy data class Foo(val bar: Bar)

interface Bar

interface Baz : Bar

class Qux : Bar

class Quux : Baz

class Corge : Baz
