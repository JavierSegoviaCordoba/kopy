package com.javiersc.kotlin.kopy.runtime

public interface Kopy<T> {

    public infix operator fun invoke(kopy: T.() -> Unit): T = TODO(error)

    public infix fun <R> R.set(other: R): R = TODO(error)

    public infix fun <R> R.set(other: (R) -> R): R = TODO(error)
}

private const val error = "Kopy plugin is not applied"
