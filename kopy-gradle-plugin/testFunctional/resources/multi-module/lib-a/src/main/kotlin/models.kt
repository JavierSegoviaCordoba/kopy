package com.javiersc.kotlin.kopy.functional.lib.a

import com.javiersc.kotlin.kopy.Kopy

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
@Kopy data class Kitchen(val cat: Cat, val squareMeters: Int)
@Kopy data class Cat(val name: String, val age: Int, val numbers: List<Int>)
