package com.javiersc.kotlin.kopy.runtime

import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate

public interface Kopyable<T> {

    @Suppress("FunctionName")
    @Deprecated("This is deprecated", level = DeprecationLevel.ERROR)
    public fun _kopy_init(): T = TODO()

    @Suppress("DEPRECATION_ERROR")
    @KopyFunctionInvoke
    public infix fun copy(copy: T.() -> Unit): T {
        val t: T = _kopy_init()
        copy(t)
        return t
    }

    @KopyFunctionInvoke public infix operator fun invoke(copy: T.() -> Unit): T = copy(copy = copy)

    @KopyFunctionSet public infix fun <D> D.set(other: D): Unit = Unit

    @KopyFunctionUpdate
    public infix fun <D> D.update(other: (D) -> D) {
        other(this)
    }
}
