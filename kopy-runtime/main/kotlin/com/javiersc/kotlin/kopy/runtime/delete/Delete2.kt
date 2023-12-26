package com.javiersc.kotlin.kopy.runtime.delete

import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.runtime.Kopyable

public fun box(): String {
    val house0 = House(street = "Street", squareMeters = 20)
    val house1 = house0.copy()
    val house2 = house1 { squareMeters.update { 40 } }
    val house3 = house1.copy(squareMeters = house1.squareMeters.let { 40 })
    return if (house2 == house3) "OK" else "ERROR"
}

@Kopy internal data class House(val street: String, val squareMeters: Int) : Kopyable<House>

internal fun delete() {
    val house0 = House(street = "Street", squareMeters = 20)
    val house1 = house0 { squareMeters.update { 2 } }
}

// internal fun House.hello() {
//    squareMeters.update { 2 }
//    val house = this {
//        squareMeters.update { 2 }
//    }
// }
