package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.gradle.testkit.test.extensions.GradleTestKitTest
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

internal class KopyGradlePluginTest : GradleTestKitTest() {

    @Test
    fun `simple setup`() = gradleTestKitTest("simple") {
        gradlew("run")
            .output
            .shouldContain("House(squareMeters=200, kitchen=Kitchen(cat=Cat(name=Felix, age=7, numbers=[2, 3, 4]), squareMeters=20))")
    }
}
