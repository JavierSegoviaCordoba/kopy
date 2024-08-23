package com.javiersc.kotlin.kopy.runtime

import com.javiersc.kotlin.kopy.KopyFunctionCopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.KopyFunctionUpdateEach
import com.javiersc.kotlin.kopy.KopyOptIn
import kotlinx.atomicfu.AtomicRef

public interface Kopyable<T> {

    @Suppress("PropertyName", "VariableNaming")
    @Deprecated("This is deprecated", level = DeprecationLevel.ERROR)
    public val _atomic: AtomicRef<T>
        get() = TODO()

    @Suppress("FunctionName")
    @Deprecated("This is deprecated", level = DeprecationLevel.ERROR)
    public fun _initKopyable(): Kopyable<T> = TODO()

    @Suppress("DEPRECATION_ERROR")
    @KopyOptIn
    @KopyFunctionCopy
    public infix fun copy(copy: T.() -> Unit): T {
        val kopyable: Kopyable<T> = _initKopyable()
        copy(kopyable._atomic.value)
        return kopyable._atomic.value
    }

    @KopyFunctionInvoke
    @KopyOptIn
    public infix operator fun invoke(copy: T.() -> Unit): T = copy(copy = copy)

    @KopyFunctionSet @KopyOptIn public infix fun <D> D.set(other: D): Unit = Unit

    @KopyFunctionUpdate
    @KopyOptIn
    public infix fun <D> D.update(other: (D) -> D) {
        other(this)
    }

    @KopyFunctionUpdateEach
    @KopyOptIn
    public infix fun <D> Iterable<D>.updateEach(other: (D) -> D) {
        map(other)
    }
}
