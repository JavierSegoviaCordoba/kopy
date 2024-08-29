// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

// MODULE: a
// FILE: a.kt
package com.javiersc.kotlin.kopy.playground.a

import com.javiersc.kotlin.kopy.Kopy
import kotlin.also
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomic

@Kopy
data class Foo(val number: Int, val letter: Char)

// MODULE: b(a)
// FILE: b.kt
package com.javiersc.kotlin.kopy.playground.b

import com.javiersc.kotlin.kopy.playground.a.Foo
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W')
    val foo20 = Foo2(number = 7, letter = 'W')

    val foo21: Foo = foo0.copy {
        number = 42
    }

    val foo22: Foo2 = foo20.copy2 {
        number.set2(42.also { _set_atomic2(_get_atomic2().copy(number = it)) })
    }

    val isOk = foo21.number == 42
    return if (isOk) "OK" else "Fail: number should be 42 but it is ${foo21.number}"
}

data class Foo2(val number: Int, val letter: Char) {

    private val atomic2: AtomicRef<Foo2> = atomic(this)

    public fun _get_atomic2(): Foo2 {
        return atomic2.value
    }

    public fun _set_atomic2(value: Foo2) {
        atomic2.lazySet(value)
    }

    public fun copy2(copy2: Foo2.() -> Unit): Foo2 {
        val tmp0 = _get_atomic2().copy()
        copy2(tmp0)
        return _get_atomic2()
    }

    public fun <T> T.set2(other: T) = Unit
}
