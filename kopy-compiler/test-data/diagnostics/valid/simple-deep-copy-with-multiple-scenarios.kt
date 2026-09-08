// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val house = House(
        squareMeters = 100,
        kitchen = Kitchen(cat = Cat(name = "Garfield", age = 5), squareMeters = 10)
    )

    val house2 = house copy {
        kitchen.cat.age.update { it + 2 }
        kitchen.squareMeters = 42
        squareMeters.set(123)
    }
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
@Kopy data class Kitchen(val cat: Cat, val squareMeters: Int)
@Kopy data class Cat(val name: String, val age: Int)

/* GENERATED_FIR_TAGS: additiveExpression, classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral,
localProperty, primaryConstructor, propertyDeclaration, stringLiteral */
