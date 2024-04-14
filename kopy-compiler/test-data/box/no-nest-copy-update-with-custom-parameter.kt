// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W')
    val foo02 = Foo2(number = 7, letter = 'W')

    val foo21 = foo0 copy { number.update { num -> num + 35 } }

    val foo22 = foo02 copy2 {
        number.update2 { num ->
            (num + 35).also { setKopyableReference2(getKopyableReference2().copy(number = it)) }
        }
    }

    val isOk = foo21.number == 42

    return if (isOk) "OK" else "Fail: number is ${foo21.number} instead of 42"
}

@Kopy data class Foo(val number: Int, val letter: Char)

data class Foo2(val number: Int, val letter: Char) : Kopyable2<Foo2> {

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
}
