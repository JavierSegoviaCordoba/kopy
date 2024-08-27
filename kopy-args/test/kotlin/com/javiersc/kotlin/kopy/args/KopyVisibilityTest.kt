package com.javiersc.kotlin.kopy.args

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KopyVisibilityTest {

    @Test
    fun `kopy visibility`() {
        KopyVisibility.from("auto") shouldBe KopyVisibility.Auto
        KopyVisibility.from("Auto") shouldBe KopyVisibility.Auto
        KopyVisibility.from("public") shouldBe KopyVisibility.Public
        KopyVisibility.from("Public") shouldBe KopyVisibility.Public
        KopyVisibility.from("internal") shouldBe KopyVisibility.Internal
        KopyVisibility.from("Internal") shouldBe KopyVisibility.Internal
        KopyVisibility.from("protected") shouldBe KopyVisibility.Protected
        KopyVisibility.from("Protected") shouldBe KopyVisibility.Protected
        KopyVisibility.from("private") shouldBe KopyVisibility.Private
        KopyVisibility.from("Private") shouldBe KopyVisibility.Private
        KopyVisibility.from("random") shouldBe KopyVisibility.Auto
    }
}
