package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
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
}
