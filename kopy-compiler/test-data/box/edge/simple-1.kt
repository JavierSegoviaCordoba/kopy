// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val roni = Pet(name = "Roni", age = 7)
    val house = House(street = "House", pet = roni)

    val house1: House = house.copy(pet = house.pet.copy(name = "Roni 2"))

    val house2: House = house copy {
        pet.name.set("Roni 2")
    }

    val isOk = house1 == house2
    return if (isOk) "OK" else "Fail: $house1 is not equal to $house2"
}

@Kopy data class House(val street: String, val pet: Pet)
@Kopy data class Pet(val name: String, val age: Int)
