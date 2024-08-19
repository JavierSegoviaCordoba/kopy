package com.javiersc.kotlin.kopy.functiona.test

import com.javiersc.kotlin.kopy.Kopy

fun main() {
    val house = House(
        squareMeters = 100,
        kitchen = Kitchen(
            cat = Cat(
                name = "Garfield",
                age = 5,
                numbers = listOf(1, 2, 3),
            ),
            squareMeters = 10,
        ),
    )
    val house2: House = house.copy {
        squareMeters = 200
        kitchen.cat.name = "Felix"
        kitchen.cat.age.update { it + 2 }
        kitchen.cat.numbers.updateEach { it + 1 }
        kitchen.squareMeters.set(20)
    }

    println(house2)
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
@Kopy data class Kitchen(val cat: Cat, val squareMeters: Int)
@Kopy data class Cat(val name: String, val age: Int, val numbers: List<Int>)
