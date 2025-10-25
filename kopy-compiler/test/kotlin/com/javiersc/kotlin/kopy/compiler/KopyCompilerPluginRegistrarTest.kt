package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.compiler.fir.FirKopyExtension
import com.javiersc.kotlin.kopy.compiler.ir.IrKopyGenerationExtension
import com.javiersc.kotlin.stdlib.endWithNewLine
import com.javiersc.kotlin.stdlib.removeDuplicateEmptyLines
import com.javiersc.kotlin.stdlib.second
import com.javiersc.kotlin.test.assertContains
import com.javiersc.kotlin.test.assertCount
import com.javiersc.kotlin.test.assertEquals
import com.javiersc.kotlin.test.assertIs
import com.javiersc.kotlin.test.assertTrue
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import org.junit.jupiter.api.Test

class KopyCompilerPluginRegistrarTest {

    @Test
    fun `check KopyCompilerPluginRegistrar uses K2`() {
        KopyCompilerPluginRegistrar().supportsK2.assertTrue()
    }

    @Test
    fun `check KopyCompilerPluginRegistrar uses Kopy extensions`() {
        val extensionStorage = CompilerPluginRegistrar.ExtensionStorage()
        val configuration = CompilerConfiguration()
        val kopyCompiler = KopyCompilerPluginRegistrar()
        extensionStorage.run { kopyCompiler.run { registerExtensions(configuration) } }
        val extensions: List<Any> = extensionStorage.registeredExtensions.values.flatten()
        extensions.assertCount(2)
        extensions.first().assertIs<FirKopyExtension>()
        extensions.second().assertIs<IrKopyGenerationExtension>()
    }

    @Test
    fun `check KopyCompilerPluginRegistrar uses Kopy measures`() {
        val extensionStorage = CompilerPluginRegistrar.ExtensionStorage()
        val configuration = CompilerConfiguration()
        val kopyCompiler = KopyCompilerPluginRegistrar()
        extensionStorage.run { kopyCompiler.run { registerExtensions(configuration) } }
        extensionStorage.disposables.firstIsInstance<MeasureStorage>().measures.assertCount(0)
    }

    @Test
    fun `check KopyConfig`() {
        val tempDir: File = createTempDirectory().toFile()
        val configuration = CompilerConfiguration()
        configuration.put(KopyKey.Debug, true)
        configuration.put(KopyKey.ReportPath, tempDir.path)
        val measureStorage = MeasureStorage()
        val kopyConfig =
            KopyConfig(
                configuration = configuration,
                measureStorage = measureStorage,
                onlyDiagnostics = false,
            )
        kopyConfig.measureStorage.put("KEY_A", "VALUE_A", 1.seconds, false)
        kopyConfig.measureStorage.put("KEY_A", "VALUE_A_A", 0.5.seconds, false)
        kopyConfig.measureStorage.put("KEY_B", "VALUE_B", 0.15.seconds, false)
        kopyConfig.measureStorage.put("KEY_C", "VALUE_C", 0.3.milliseconds, false)
        kopyConfig.measureStorage.put("KEY_C", "VALUE_C_C", 0.6.milliseconds, false)
        kopyConfig.measureStorage.put("KEY_C", "VALUE_C_C_C", 0.8.milliseconds, false)
        kopyConfig.createReport()
        tempDir
            .resolve("metrics")
            .walkTopDown()
            .first { it.isFile }
            .readText()
            .replace("\r\n", "\n")
            .removeDuplicateEmptyLines()
            .endWithNewLine()
            .assertEquals(
                """
                |KEY_A:
                |  - VALUE_A, 1s
                |  - VALUE_A_A, 500ms
                |KEY_B:
                |  - VALUE_B, 150ms
                |KEY_C:
                |  - VALUE_C_C_C, 800us
                |  - VALUE_C_C, 600us
                |  - VALUE_C, 300us
                |
                |TOTAL: 1.651700s
                """
                    .trimMargin()
                    .removeDuplicateEmptyLines()
                    .endWithNewLine()
            )
        kopyConfig.measureExecution("KEY_Y", isNested = true) { "VALUE_Y" }
        kopyConfig.measureExecution("KEY_Z") { "VALUE_Z" }
        kopyConfig.measureStorage.measures
            .map { Triple(it.key, it.value, it.isNested) }
            .assertContains(Triple("KEY_Y", "VALUE_Y", true))
            .assertContains(Triple("KEY_Z", "VALUE_Z", false))
    }
}
