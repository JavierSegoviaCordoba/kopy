// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

// FILE: a.kt

package kotlinx.serialization

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Serializable

@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class Transient

// FILE: b.kt

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

fun box(): String {
    val foo = Foo(bar = Bar(age = 41))

    val foo2: Foo = foo copy {
        bar.age.set(42)
    }

    val isOk = foo2.bar.age == 42
    return if (isOk) "OK" else "Fail: $foo2"
}

@Kopy data class Foo(val bar: Bar)
@Kopy @Serializable data class Bar(val age: Int)
