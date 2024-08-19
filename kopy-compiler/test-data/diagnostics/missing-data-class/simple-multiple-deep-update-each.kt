// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun diagnostics() {
    val house = House(
        squareMeters = 100,
        kitchen = Kitchen(
            cat = Cat(
                name = "Garfield",
                ages = listOf(5, 7),
            ),
            squareMeters = listOf(10, 13),
        )
    )

    val house2 = house copy {
        <!MISSING_DATA_CLASS!>kitchen<!>.cat.ages.updateEach { it + 6 }
        <!MISSING_DATA_CLASS!>kitchen<!>.squareMeters.updateEach { it + 45 }
    }
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
class Kitchen(val cat: Cat, val squareMeters: List<Int>)
data class Cat(val name: String, val ages: List<Int>)