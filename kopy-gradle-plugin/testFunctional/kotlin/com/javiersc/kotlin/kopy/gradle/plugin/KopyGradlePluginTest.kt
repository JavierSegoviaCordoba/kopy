@file:Suppress("FunctionName", "FunctionNaming", "MaxLineLength")

package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.gradle.testkit.test.extensions.GradleTestKitTest
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

internal class KopyGradlePluginTest : GradleTestKitTest() {

    @Test
    fun `kotlin multiplatform`() =
        gradleTestKitTest("kotlin-multiplatform") {
            gradlew("assemble").output.shouldContain("BUILD SUCCESSFUL")
        }

    @Test
    fun `kotlin plugin atomicfu`() =
        gradleTestKitTest("kotlin-plugin-atomicfu") {
            gradlew("run")
                .output
                .shouldContain(
                    "House(squareMeters=200, kitchen=Kitchen(cat=Cat(name=Felix, age=7, numbers=[2, 3, 4]), squareMeters=20))"
                )
        }

    @Test
    fun `multi-module`() =
        gradleTestKitTest("multi-module") {
            gradlew("run")
                .output
                .shouldContain(
                    "House(squareMeters=200, kitchen=Kitchen(cat=Cat(name=Felix, age=7, numbers=[2, 3, 4]), squareMeters=20))"
                )
        }

    @Test
    fun `simple setup`() =
        gradleTestKitTest("simple") {
            gradlew("run")
                .output
                .shouldContain(
                    "House(squareMeters=200, kitchen=Kitchen(cat=Cat(name=Felix, age=7, numbers=[2, 3, 4]), squareMeters=20))"
                )
            projectDir
                .resolve("build/reports/kopy/metrics")
                .walkTopDown()
                .filter { it.isFile }
                .first()
                .name
                .shouldContain("metrics_")
        }

    @Test
    fun `simple-custom-reports-dir setup`() =
        gradleTestKitTest("simple-custom-reports-dir") {
            gradlew("run")
                .output
                .shouldContain(
                    "House(squareMeters=200, kitchen=Kitchen(cat=Cat(name=Felix, age=7, numbers=[2, 3, 4]), squareMeters=20))"
                )
            projectDir
                .resolve("build/reports/custom/kopy/metrics")
                .walkTopDown()
                .filter { it.isFile }
                .first()
                .name
                .shouldContain("metrics_")
        }

    @Test
    fun `private visibility`() =
        gradleTestKitTest("private-visibility") {
            withArguments("run")
                .buildAndFail()
                .output
                .shouldContain(
                    "Cannot access 'fun copy(copy: House.() -> Unit): House': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
                .shouldContain(
                    "Cannot access 'fun <S> S.set(other: S): Unit': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
                .shouldContain(
                    "Cannot access 'fun <S> S.set(other: S): Unit': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
                .shouldContain(
                    "Cannot access 'fun <U> U.update(transform: (U) -> U): Unit': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
                .shouldContain(
                    "Cannot access 'fun <UE> Iterable<UE>.updateEach(transform: (UE) -> UE): Unit': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
                .shouldContain(
                    "Cannot access 'fun <S> S.set(other: S): Unit': it is private in 'com.javiersc.kotlin.kopy.functional.test.House'."
                )
        }
}
