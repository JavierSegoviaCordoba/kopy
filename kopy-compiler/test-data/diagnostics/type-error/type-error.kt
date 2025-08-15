// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -MISSING_DEPENDENCY_CLASS -MISSING_DEPENDENCY_SUPERCLASS
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

fun box(): String {
    val house = House(street = "foo", names = listOf("bar"))

    val house2: House = house copy {
        street.set(<!ARGUMENT_TYPE_MISMATCH!>42<!>)
        street = <!ARGUMENT_TYPE_MISMATCH!>42<!>
        street.set("foo2")
        street = "foo2"
        street.update { "$it 2" }
        street.update { <!RETURN_TYPE_MISMATCH!>42<!> }
        names.updateEach { "$it 2" }
        names.updateEach { <!RETURN_TYPE_MISMATCH!>42<!> }
    }

    return "OK"
}

@Kopy data class House(val street: String, val names: List<String>)
