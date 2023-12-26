@file:Suppress("DEPRECATION_ERROR")

package com.javiersc.kotlin.kopy.runtime

import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import java.util.concurrent.atomic.AtomicReference

public interface Kopyable<M> {

    @KopyFunctionInvoke
    public infix operator fun invoke(copy: context(KopyableScope<M>) M.() -> Unit): M {
        val scope = KopyableScope<M>(kopyError())
        copy(scope, scope.getKopyableReference())
        return scope.getKopyableReference()
    }

    @KopyFunctionInvoke
    public infix fun copy(copy: context(KopyableScope<M>) M.() -> Unit): M = invoke(copy)
}

@Deprecated("This is deprecated", level = DeprecationLevel.ERROR)
public class KopyableScope<M> internal constructor(data: M) {

    private val atomic = AtomicReference(data)

    public fun getKopyableReference(): M = atomic.get()

    public fun setKopyableReference(other: M) {
        atomic.set(other)
    }

    context(KopyableScope<M>)
    @KopyFunctionSet
    public infix fun <D> D.set(other: D): Unit = Unit

    context(KopyableScope<M>)
    @KopyFunctionUpdate
    public infix fun <D> D.update(other: (D) -> D) {
        other(this)
    }
}

private fun <T> kopyError(): T = TODO(error)

private const val error = "Kopy plugin is not applied"
