// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo0 = Foo(number = 7, letter = 'W')
    val foo02 = Foo2(number = 7, letter = 'W')

    val foo21 = foo0 copy { number.set(42) }

    val foo22 = foo02 copy {
        number.set(42.also { setKopyableReference(getKopyableReference().copy(number = it)) })
    }

    val isOk = foo21.number == 42

    return if (isOk) "OK" else "Fail: number is ${foo21.number} instead of 42"
}

@Kopy data class Foo(val number: Int, val letter: Char)

data class Foo2(val number: Int, val letter: Char) {

    public infix operator fun invoke(copy: context(KopyableScope2<Foo2>) Foo2.() -> Unit): Foo2 {
        val scope = KopyableScope2<Foo2>(this)
        copy(scope, scope.getKopyableReference())
        return scope.getKopyableReference()
    }

    public infix fun copy(copy: context(KopyableScope2<Foo2>) Foo2.() -> Unit): Foo2 = invoke(copy)
}

public class KopyableScope2<M> internal constructor(data: M) {

    private var _data: M = data

    public fun getKopyableReference(): M = _data

    public fun setKopyableReference(other: M) {
        _data = other
    }

    public infix fun <D> D.set(other: D): Unit = Unit

    public infix fun <D> D.update(other: (D) -> D) {
        other(this)
    }
}
