// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo = Foo(bar = Bar.Baz)
    val foo2 = foo.copy { bar = Bar.Qux }
    val foo3 = foo.copy { bar = Bar.Corge.Grault.Garply }

    val isOk: Boolean =
        foo.bar is Bar.Baz && foo2.bar is Bar.Qux && foo3.bar is Bar.Corge.Grault.Garply

    return if (isOk) "OK" else "Error"
}

@Kopy data class Foo(val bar: Bar)

sealed class Bar {
    data object Baz : Bar()

    data object Qux : Bar()

    sealed class Corge : Bar() {
        sealed class Grault : Corge() {
            data object Garply : Grault()
        }
    }
}
