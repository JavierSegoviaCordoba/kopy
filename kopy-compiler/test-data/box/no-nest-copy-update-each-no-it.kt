// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

fun box(): String {
    val foo0 = Foo(numbers = listOf(7, 3), letters = listOf('W'))
    val foo02 = Foo2(numbers = listOf(7, 3), letters = listOf('W'))

    val foo21 = foo0 copy {
        numbers.updateEach { 42 }
    }

    val foo22 = foo02 copy2 {
        numbers.update2 { it.map { 42 } }
    }

    val foo23 = foo02 copy2 {
        numbers.update2 {
            it.map { 42 }.also { setKopyableReference2(getKopyableReference2().copy(numbers = it)) }
        }
    }

    val isOk = foo21.numbers.all { it == 42 }

    return if (isOk) "OK" else "Fail: numbers are ${foo21.numbers} instead of all being 42"
}

@Kopy data class Foo(val numbers: List<Int>, val letters: List<Char>)

data class Foo2(val numbers: List<Int>, val letters: List<Char>) : Kopyable2<Foo2> {

    override val _atomic2: AtomicRef<Foo2> = atomic(this)

    override fun _initKopyable2(): Kopyable2<Foo2> = this.copy()
}

public interface Kopyable2<T> {

    public val _atomic2: AtomicRef<T>

    public fun _initKopyable2(): Kopyable2<T>

    public fun getKopyableReference2(): T = _atomic2.value

    public fun setKopyableReference2(value: T) {
        _atomic2.lazySet(value)
    }

    public infix fun copy2(copy2: T.() -> Unit): T {
        val kopyable: Kopyable2<T> = _initKopyable2()
        copy2(kopyable._atomic2.value)
        return kopyable._atomic2.value
    }

    public infix fun <D> D.set2(other: D): Unit = Unit

    public infix fun <D> D.update2(other: (D) -> D) {
        other(this)
    }

    public infix fun <D> Iterable<D>.updateEach2(other: (D) -> D) {
        map(other)
    }
}
