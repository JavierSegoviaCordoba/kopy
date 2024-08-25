package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.gradle.project.test.extensions.GradleProjectTest
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
            val kopy: KopyGradlePlugin = plugins.getPlugin(KopyGradlePlugin::class.java)
            kopy.getCompilerPluginId().shouldBe("com.javiersc.kotlin.kopy-compiler")
            kopy.getPluginArtifact().apply {
                groupId shouldBe "com.javiersc.kotlin"
                artifactId shouldBe "kopy-compiler"
                version shouldBe KopyCompilerProjectData.Version
            }
        }
    }

    @Test
    fun `kotlin jvm`() {
        gradleProjectTest {
            pluginManager.apply("com.javiersc.kotlin.kopy")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            extensions.findByName("kopy").shouldNotBeNull()
            extensions.findByType(KopyExtension::class.java).shouldNotBeNull()
            val kopy: KopyGradlePlugin = plugins.getPlugin(KopyGradlePlugin::class.java)
            kopy.getCompilerPluginId().shouldBe("com.javiersc.kotlin.kopy-compiler")
            kopy.getPluginArtifact().apply {
                groupId shouldBe "com.javiersc.kotlin"
                artifactId shouldBe "kopy-compiler"
                version shouldBe KopyCompilerProjectData.Version
            }
        }
    }

    @Test
    fun `kotlin multiplatform`() {
        gradleProjectTest {
            pluginManager.apply("com.javiersc.kotlin.kopy")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            extensions.findByName("kopy").shouldNotBeNull()
            extensions.findByType(KopyExtension::class.java).shouldNotBeNull()
            val kopy: KopyGradlePlugin = plugins.getPlugin(KopyGradlePlugin::class.java)
            kopy.getCompilerPluginId().shouldBe("com.javiersc.kotlin.kopy-compiler")
            kopy.getPluginArtifact().apply {
                groupId shouldBe "com.javiersc.kotlin"
                artifactId shouldBe "kopy-compiler"
                version shouldBe KopyCompilerProjectData.Version
            }
        }
    }
}
