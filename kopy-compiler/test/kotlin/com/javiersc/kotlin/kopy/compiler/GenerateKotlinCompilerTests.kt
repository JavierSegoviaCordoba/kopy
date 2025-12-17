package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.compiler.test.runners.JvmBoxTest
import com.javiersc.kotlin.compiler.test.runners.JvmDiagnosticTest
import com.javiersc.kotlin.compiler.test.services.MetaRuntimeClasspathProvider
import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.configureFirParser
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.junit.jupiter.api.BeforeAll

fun main() = generateTestGroupSuiteWithJUnit5 {
    testGroup(testDataRoot = "test-data", testsRoot = "test-gen/java") {
        testClass<AbstractDiagnosticTest> { model("diagnostics") }
        testClass<AbstractBoxTest> { model("box") }
        testClass<AbstractKopy0AutoDiagnosticTest> { model("diagnostics-kopy-visibility/0_auto") }
        testClass<AbstractKopy1PublicDiagnosticTest> {
            model("diagnostics-kopy-visibility/1_public")
        }
        testClass<AbstractKopy2InternalDiagnosticTest> {
            model("diagnostics-kopy-visibility/2_internal")
        }
        testClass<AbstractKopy3ProtectedDiagnosticTest> {
            model("diagnostics-kopy-visibility/3_protected")
        }
        testClass<AbstractKopy4PrivateDiagnosticTest> {
            model("diagnostics-kopy-visibility/4_private")
        }
        testClass<AbstractKopyAllDiagnosticTest> { model("diagnostics-kopy-functions/all") }
        testClass<AbstractKopyCopyDiagnosticTest> { model("diagnostics-kopy-functions/copy") }
        testClass<AbstractKopyInvokeDiagnosticTest> { model("diagnostics-kopy-functions/invoke") }
    }
}

open class AbstractDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractBoxTest : JvmBoxTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        // configuration.put(KopyKey.Debug, true)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = false)
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration()
        }
    }
}

open class AbstractKopy0AutoDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Auto)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopy1PublicDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Public)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopy2InternalDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Internal)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopy3ProtectedDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Protected)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopy4PrivateDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Private)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopyAllDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.CopyFunctions, KopyCopyFunctions.entries)
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopyCopyDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.CopyFunctions, listOf(KopyCopyFunctions.Copy))
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

open class AbstractKopyInvokeDiagnosticTest : KopyDiagnosticTest() {

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        configuration.put(KopyKey.CopyFunctions, listOf(KopyCopyFunctions.Invoke))
        registerAllExtensions(configuration = configuration, onlyDiagnostics = true)
    }
}

abstract class KopyDiagnosticTest : JvmDiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        builder.configureFirParser(FirParser.Psi)
        super.configure(builder)
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration()
        }
    }
}
