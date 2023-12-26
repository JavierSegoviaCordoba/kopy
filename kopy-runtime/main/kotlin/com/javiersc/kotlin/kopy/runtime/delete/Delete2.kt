@file:Suppress("DEPRECATION_ERROR")

package com.javiersc.kotlin.kopy.runtime.delete

import com.javiersc.kotlin.kopy.runtime.KopyableScope

internal fun box() {
    val d = D(num = 0)
    val c = C(d = d, text = "Text")
    val b = B(c = c, isValid = true)
    val a = A(b = b, letter = 'A')

    val a1 = a kopy {
        b.isValid.set(false)
        b.c.text.update { "$it Random 2" }
    }
}

//@Kopy data class House(val street: String, val squareMeters: Int)

internal data class A(val b: B, val letter: Char) {

    infix fun kopy(kopy: context(KopyableScope<A>) A.() -> Unit): A {
        val scope: KopyableScope<A> = KopyableScope(this)
        kopy(scope, scope.getKopyableReference())
        return scope.getKopyableReference()
    }
}
internal data class B(val c: C, val isValid: Boolean)
internal data class C(val d: D, val text: String)
internal data class D(val num: Int)
