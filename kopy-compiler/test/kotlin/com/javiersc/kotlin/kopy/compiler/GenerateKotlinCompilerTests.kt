package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.compiler.test.runners.BoxTest
import com.javiersc.kotlin.compiler.test.runners.DiagnosticTest
import com.javiersc.kotlin.compiler.test.services.MetaRuntimeClasspathProvider
import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.model.TestModule

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testDataRoot = "test-data", testsRoot = "test-gen/java") {
            testClass<AbstractDiagnosticTest> { model("diagnostics") }
            testClass<AbstractBoxTest> { model("box") }
            testClass<AbstractKopy0AutoDiagnosticTest> { model("diagnostics-kopy-visibility/0_auto") }
            testClass<AbstractKopy1PublicDiagnosticTest> { model("diagnostics-kopy-visibility/1_public") }
            testClass<AbstractKopy2InternalDiagnosticTest> { model("diagnostics-kopy-visibility/2_internal") }
            testClass<AbstractKopy3ProtectedDiagnosticTest> { model("diagnostics-kopy-visibility/3_protected") }
            testClass<AbstractKopy4PrivateDiagnosticTest> { model("diagnostics-kopy-visibility/4_private") }
            testClass<AbstractKopyAllDiagnosticTest> { model("diagnostics-kopy-functions/all") }
            testClass<AbstractKopyCopyDiagnosticTest> { model("diagnostics-kopy-functions/copy") }
            testClass<AbstractKopyInvokeDiagnosticTest> { model("diagnostics-kopy-functions/invoke") }
        }
    }
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

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        allExtensions(configuration)
    }
}

open class AbstractKopy0AutoDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Auto.value)
        allExtensions(configuration)
    }
}

open class AbstractKopy1PublicDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Public.value)
        allExtensions(configuration)
    }
}

open class AbstractKopy2InternalDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Internal.value)
        allExtensions(configuration)
    }
}

open class AbstractKopy3ProtectedDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Protected.value)
        allExtensions(configuration)
    }
}

open class AbstractKopy4PrivateDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Visibility, KopyVisibility.Private.value)
        allExtensions(configuration)
    }
}

open class AbstractKopyAllDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Functions, KopyFunctions.All.value)
        allExtensions(configuration)
    }
}

open class AbstractKopyCopyDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Functions, KopyFunctions.Copy.value)
        allExtensions(configuration)
    }
}

open class AbstractKopyInvokeDiagnosticTest : DiagnosticTest() {

    override val runtimeClasspathProvider: Constructor<MetaRuntimeClasspathProvider> =
        ::GeneratedMetaRuntimeClasspathProvider

    override fun ExtensionStorage.registerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        configuration.put(KopyKey.Functions, KopyFunctions.Invoke.value)
        allExtensions(configuration)
    }
}
