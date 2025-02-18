package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.gradle.project.test.extensions.GradleProjectTest
import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
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
            val kotlin: KotlinMultiplatformExtension =
                extensions.getByType(KotlinMultiplatformExtension::class)
            configure<KotlinMultiplatformExtension> { jvm() }
            kopy.getCompilerPluginId().shouldBe("com.javiersc.kotlin.kopy-compiler")
            kopy.getPluginArtifact().apply {
                groupId shouldBe "com.javiersc.kotlin"
                artifactId shouldBe "kopy-compiler"
                version shouldBe KopyCompilerProjectData.Version
            }
            val jvmCompilation: KotlinCompilation<*> =
                kotlin.targets.first { it.name.contains("jvm") }.compilations.first()
            kopy.isApplicable(jvmCompilation).shouldBeTrue()
            kopy
                .applyToCompilation(jvmCompilation)
                .get()
                .map { it.key to it.value }
                .shouldBe(
                    listOf(
                        KopyFunctions.NAME to KopyFunctions.All.value,
                        KopyVisibility.NAME to KopyVisibility.Auto.value,
                    )
                )
        }
    }
}
