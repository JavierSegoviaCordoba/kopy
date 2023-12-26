@file:Suppress("DEPRECATION_ERROR")

package com.javiersc.kotlin.kopy.runtime

import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate

public interface Kopyable<T> {

    @KopyFunctionInvoke
    public infix operator fun invoke(kopy: /*context(KopyableScope)*/ T.() -> Unit): T = TODO(error)

    @KopyFunctionInvoke
    public infix fun copy(kopy: /*context(KopyableScope)*/ T.() -> Unit): T = TODO(error)

    // context(KopyableScope)
    @KopyFunctionSet public infix fun <R> R.set(other: R): R = TODO(error)

    // context(KopyableScope)
    @KopyFunctionUpdate public infix fun <R> R.update(other: (R) -> R): R = TODO(error)

    @Deprecated("This is deprecated", level = DeprecationLevel.ERROR)
    public interface KopyableScope
}

private const val error = "Kopy plugin is not applied"
