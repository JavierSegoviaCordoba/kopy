package com.javiersc.kotlin.kopy.args

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KopyFunctionsTest {

    @Test
    fun kopy_functions() {
        KopyFunctions.from("all") shouldBe KopyFunctions.All
        KopyFunctions.from("All") shouldBe KopyFunctions.All
        KopyFunctions.from("copy") shouldBe KopyFunctions.Copy
        KopyFunctions.from("Copy") shouldBe KopyFunctions.Copy
        KopyFunctions.from("invoke") shouldBe KopyFunctions.Invoke
        KopyFunctions.from("Invoke") shouldBe KopyFunctions.Invoke
        KopyFunctions.from("random") shouldBe KopyFunctions.All
    }
}
