package com.javiersc.kotlin.kopy.functional.test.lib.a

import com.javiersc.kotlin.kopy.Kopy

fun a() {
    val house = House(
        squareMeters = 53,
        kitchen = Kitchen(
            squareMeters = 10,
        ),
    )
    house.copy {
        kitchen.squareMeters = 77
    }
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
@Kopy data class Kitchen(val squareMeters: Int)
