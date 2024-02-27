// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val foo0 = Foo2(number = 7, letter = 'W', text = "Random")
    val foo1 = Foo(number = 7, letter = 'W', text = "Random")

    val foo01 = foo0 copy2 {
        text.set2("Replaced".also { text = it })
    }
    val foo11 = foo1 copy {
        text.set("Replaced")
    }

    val isOk = foo11.text == "Replaced"

    return if (isOk) "OK" else "Fail: text is ${foo11.text} instead of `Replaced`"
}

@Kopy data class Foo(val number: Int, val letter: Char, var text: String)

data class Foo2(val number: Int, val letter: Char, var text: String) : Kopyable2<Foo2> {

    public override fun _kopy_init2(): Foo2 = this.copy()
}

public interface Kopyable2<T> {

    public fun _kopy_init2(): T = TODO()

    public infix fun copy2(copy: T.() -> Unit): T {
        val t: T = _kopy_init2()
        copy(t)
        return t
    }

    public infix fun <D> D.set2(other: D): Unit = Unit

    public infix fun <D> D.update2(other: (D) -> D) {
        other(this)
    }
}
