// !LANGUAGE: +ContextReceivers
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val house = House(
        squareMeters = 100,
        kitchen = Kitchen(cat = Cat(name = "Garfield", age = 5), squareMeters = 10)
    )

    val house1 = house.copy(
        kitchen = house.kitchen.copy(
            cat = house.kitchen.cat.copy(
                age = 6,
            ),
            squareMeters = 45,
        ),
    )

    val house2 = house copy {
        kitchen.cat.age.set(6)
        kitchen.squareMeters.set(45)
    }

    val isOk = house1 == house2 && house != house2
    return if (isOk) "OK" else "Fail:\n  - house0: $house \n  - house1: $house1 \n  - house2: $house2"
}

@Kopy data class House(val squareMeters: Int, val kitchen: Kitchen)
@Kopy data class Kitchen(val cat: Cat, val squareMeters: Int)
@Kopy data class Cat(val name: String, val age: Int)
