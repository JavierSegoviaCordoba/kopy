package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class KopyGradlePlugin
@Inject
constructor(
    private val providers: ProviderFactory,
) : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.createExtension()
        target.withKotlin { suppressKopyOptInt() }
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = providers.provider { emptyList() }

    override fun getCompilerPluginId(): String =
        "${KopyCompilerProjectData.Group}.${KopyCompilerProjectData.Name}"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = KopyCompilerProjectData.Group,
            artifactId = KopyCompilerProjectData.Name,
            version = KopyCompilerProjectData.Version,
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    private fun Project.createExtension() {
        extensions.create<KopyExtension>(
            KopyExtension.NAME,
        )
    }

    private fun Project.withKotlin(action: Project.() -> Unit) {
        pluginManager.withPlugin("org.jetbrains.kotlin.android") { action() }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") { action() }
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") { action() }
    }

    private fun Project.suppressKopyOptInt() {
        configure<KotlinProjectExtension> {
            sourceSets.configureEach { sourceSet ->
                sourceSet.languageSettings.optIn("com.javiersc.kotlin.kopy.KopyOptIn")
            }
        }
    }
}
