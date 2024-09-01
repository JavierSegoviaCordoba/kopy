// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val address1 = Address(street = "Street", city = "City", state = "State", zip = "Zip")
    val name1 = Name(first = "First", last = "Last")
    val privateInfo1 = PrivateInfo(ssn = "SSN", dob = "DOB")
    val person1 = Person(name = name1, address = address1, privateInfo = privateInfo1)

    val person2 = person1 copy {
        privateInfo.ssn.set("SSN 42")
    }

    val isOk = person2.privateInfo.ssn == "SSN 42"
    return if (isOk) "OK" else "Fail: ${person2.privateInfo.ssn} is not equal to SSN 42"
}

@Kopy data class Person(val name: Name, val address: Address, val privateInfo: PrivateInfo)

@Kopy data class Name(val first: String, val last: String)

@Kopy data class Address(val street: String, val city: String, val state: String, val zip: String)

data class PrivateInfo(val ssn: String, val dob: String)
