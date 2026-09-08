// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val house = House(
        squareMeters = 100,
        kitchen = Kitchen(
            cat = Cat(
                name = "Garfield",
                age = 5,
            ),
            squareMeters = 10,
        )
    )

    val house2 = house copy {
        <!MISSING_DATA_CLASS!>kitchen<!>.cat.age.update { it + 6 }
        <!MISSING_DATA_CLASS!>kitchen<!>.squareMeters.update { it + 45 }
    }
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
class Kitchen(val cat: Cat, val squareMeters: Int)
data class Cat(val name: String, val age: Int)

/* GENERATED_FIR_TAGS: additiveExpression, classDeclaration, data, functionDeclaration, integerLiteral, lambdaLiteral,
localProperty, primaryConstructor, propertyDeclaration, stringLiteral */
