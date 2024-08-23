package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.gradle.project.test.extensions.GradleProjectTest
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

internal class KopyGradlePluginIntegrationTest : GradleProjectTest() {

    @Test
    fun `kotlin android`() {
        gradleProjectTest {
            pluginManager.apply("com.javiersc.kotlin.kopy")
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            extensions.findByName("kopy").shouldNotBeNull()
            extensions.findByType(KopyExtension::class.java).shouldNotBeNull()
        }
    }

    @Test
    fun `kotlin jvm`() {
        gradleProjectTest {
            pluginManager.apply("com.javiersc.kotlin.kopy")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            extensions.findByName("kopy").shouldNotBeNull()
            extensions.findByType(KopyExtension::class.java).shouldNotBeNull()
        }
    }

    @Test
    fun `kotlin multiplatform`() {
        gradleProjectTest {
            pluginManager.apply("com.javiersc.kotlin.kopy")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            extensions.findByName("kopy").shouldNotBeNull()
            extensions.findByType(KopyExtension::class.java).shouldNotBeNull()
        }
    }
}
