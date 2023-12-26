package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.runtime.Kopyable

internal fun box(): String {
    val house0 = House(street = "Street", squareMeters = 20)
    val house1 = house0 { squareMeters.update { 40 } }
    val house2 =
        house0.run {
            var tmp1 = this
            tmp1 = tmp1.copy(squareMeters = tmp1.squareMeters.let { 40 })
            tmp1
        }
    return if (house1 == house2) "OK" else "ERROR"
}

@Kopy internal data class House(val street: String, val squareMeters: Int) : Kopyable<House>
