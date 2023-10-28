package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.compiler.test.generateKotlinCompilerTests
import com.javiersc.kotlin.compiler.test.runners.BoxTest
import com.javiersc.kotlin.compiler.test.runners.DiagnosticTest
import com.javiersc.kotlin.compiler.test.services.MetaRuntimeClasspathProvider
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives
import org.jetbrains.kotlin.test.model.TestModule

fun main() {
    generateKotlinCompilerTests<AbstractDiagnosticTest, AbstractBoxTest>()
}

open class AbstractDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        allExtensions(configuration)
    }
}

open class AbstractBoxTest : BoxTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        builder.defaultDirectives { LanguageSettingsDirectives.LANGUAGE with "+ContextReceivers" }
        super.configure(builder)
    }

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        allExtensions(configuration)
    }
}
