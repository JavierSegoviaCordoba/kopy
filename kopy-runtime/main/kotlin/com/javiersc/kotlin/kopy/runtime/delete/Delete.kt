//package com.javiersc.kotlin.kopy.playground
////
//import com.javiersc.kotlin.kopy.Kopy
//import com.javiersc.kotlin.kopy.runtime.Kopyable
////
////internal fun box(): String {
////    val house0 = House(street = "Street", squareMeters = 20)
////    val house1 = house0 { squareMeters.update { it + 20 } }
////    val house2 =
////        house0.run {
////            var tmp0 = this
////            tmp0 = tmp0.copy(squareMeters = tmp0.squareMeters.let { it + 20 })
////            tmp0
////        }
////    return if (house1 == house2) "OK" else "Fail: \nHouse1: $house1 \nHouse2: $house2"
////}
////
//
//internal fun diagnostics() {
//    val house0 = House(street = "Street", squareMeters = 20)
//
//    val house1 = house0 kopy {}
//
//    val house2 = house0 kopy { squareMeters.set(40) }
//}
//
//@Kopy internal data class House(val street: String, val squareMeters: Int) : Kopyable<House>
