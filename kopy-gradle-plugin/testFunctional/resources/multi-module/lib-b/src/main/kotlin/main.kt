package com.javiersc.kotlin.kopy.functional.lib.b

import com.javiersc.kotlin.kopy.functional.lib.a.House
import com.javiersc.kotlin.kopy.functional.lib.a.Kitchen
import com.javiersc.kotlin.kopy.functional.lib.a.Cat

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
