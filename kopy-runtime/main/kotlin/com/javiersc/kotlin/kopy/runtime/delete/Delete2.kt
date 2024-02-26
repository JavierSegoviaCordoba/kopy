// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

internal fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W')
    val foo02 = Foo2(number = 7, letter = 'W')

    val foo21 = foo0

    val foo22 = foo02 copy {
        number.set(42.also { setKopyableReference(getKopyableReference().copy(number = it)) })
    }

    val isOk = foo21.number == 42

    return if (isOk) "OK" else "Fail: number is ${foo21.number} instead of 42"
}

internal data class Foo(val number: Int, val letter: Char)

internal data class Foo2(val number: Int, val letter: Char) : Kopyable2<Foo2> {

    override val _atomic: AtomicRef<Foo2> = atomic(this)

    override fun _initKopyable(): Kopyable2<Foo2> = this.copy()
}

public interface Kopyable2<T> {

    public val _atomic: AtomicRef<T>

    public fun _initKopyable(): Kopyable2<T>

    public fun getKopyableReference(): T = _atomic.value

    public fun setKopyableReference(value: T) {
        _atomic.lazySet(value)
    }

    public infix fun copy(copy: T.() -> Unit): T {
        val kopyable: Kopyable2<T> = _initKopyable()
        copy(kopyable._atomic.value)
        return kopyable._atomic.value
    }

    public infix operator fun invoke(copy: T.() -> Unit): T = copy(copy)

    public infix fun <D> D.set(other: D): Unit = Unit

    public infix fun <D> D.update(other: (D) -> D) {
        other(this)
    }
}
