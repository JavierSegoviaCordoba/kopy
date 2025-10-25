package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyDebug
import com.javiersc.kotlin.kopy.args.KopyReportPath
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyKey.TransformFunctions
import com.javiersc.kotlin.stdlib.fifth
import com.javiersc.kotlin.stdlib.forth
import com.javiersc.kotlin.stdlib.second
import com.javiersc.kotlin.stdlib.third
import com.javiersc.kotlin.test.assertContainsExactly
import com.javiersc.kotlin.test.assertCount
import com.javiersc.kotlin.test.assertEquals
import com.javiersc.kotlin.test.assertFalse
import com.javiersc.kotlin.test.assertNotNull
import com.javiersc.kotlin.test.assertTrue
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.junit.jupiter.api.Test

class KopyCommandLineProcessorTest {

    @Test
    fun `check plugin id`() {
        val processor = KopyCommandLineProcessor()
        processor.pluginId.assertEquals("com.javiersc.kotlin.kopy-compiler")
    }

    @Test
    fun `check plugin options`() {
        val processor = KopyCommandLineProcessor()
        val pluginOptions: Collection<AbstractCliOption> = processor.pluginOptions
        pluginOptions.assertCount(5)
        pluginOptions.first().optionName.assertEquals(KopyDebug.NAME)
        pluginOptions.second().optionName.assertEquals(KopyCopyFunctions.NAME)
        pluginOptions.third().optionName.assertEquals(KopyReportPath.NAME)
        pluginOptions.forth().optionName.assertEquals(KopyTransformFunctions.NAME)
        pluginOptions.fifth().optionName.assertEquals(KopyVisibility.NAME)

        processor.testOption(option = pluginOptions.first(), value = "${true}") { configuration ->
            configuration[KopyKey.Debug].assertNotNull().assertTrue()
        }

        processor.testOption(option = pluginOptions.first(), value = "${false}") { configuration ->
            configuration[KopyKey.Debug].assertNotNull().assertFalse()
        }

        processor.testOption(
            option = pluginOptions.second(),
            values = KopyCopyFunctions.entries.map(KopyCopyFunctions::value),
        ) { configuration ->
            configuration[KopyKey.CopyFunctions]
                .assertNotNull()
                .assertContainsExactly(KopyCopyFunctions.entries)
        }

        processor.testOption(
            option = pluginOptions.second(),
            value = KopyCopyFunctions.Copy.value,
        ) { configuration ->
            configuration[KopyKey.CopyFunctions]
                .assertNotNull()
                .assertContainsExactly(listOf(KopyCopyFunctions.Copy))
        }

        processor.testOption(
            option = pluginOptions.second(),
            value = KopyCopyFunctions.Invoke.value,
        ) { configuration ->
            configuration[KopyKey.CopyFunctions]
                .assertNotNull()
                .assertContainsExactly(listOf(KopyCopyFunctions.Invoke))
        }

        processor.testOption(option = pluginOptions.third(), value = "build/reports/kopy") {
            configuration ->
            configuration[KopyKey.ReportPath].assertEquals("build/reports/kopy")
        }

        processor.testOption(
            option = pluginOptions.forth(),
            value = KopyTransformFunctions.Set.value,
        ) { configuration ->
            configuration[TransformFunctions]
                .assertNotNull()
                .assertContainsExactly(listOf(KopyTransformFunctions.Set))
        }

        processor.testOption(
            option = pluginOptions.forth(),
            values = KopyTransformFunctions.entries.map(KopyTransformFunctions::value),
        ) { configuration ->
            configuration[TransformFunctions]
                .assertNotNull()
                .assertContainsExactly(
                    listOf(
                        KopyTransformFunctions.Set,
                        KopyTransformFunctions.Update,
                        KopyTransformFunctions.UpdateEach,
                    )
                )
        }

        processor.testOption(option = pluginOptions.fifth(), value = KopyVisibility.Auto.value) {
            configuration ->
            configuration[KopyKey.Visibility].assertEquals(KopyVisibility.Auto)
        }

        processor.testOption(option = pluginOptions.fifth(), value = KopyVisibility.Public.value) {
            configuration ->
            configuration[KopyKey.Visibility].assertEquals(KopyVisibility.Public)
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Internal.value,
        ) { configuration ->
            configuration[KopyKey.Visibility].assertEquals(KopyVisibility.Internal)
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Protected.value,
        ) { configuration ->
            configuration[KopyKey.Visibility].assertEquals(KopyVisibility.Protected)
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Private.value,
        ) { configuration ->
            configuration[KopyKey.Visibility].assertEquals(KopyVisibility.Private)
        }
    }

    private fun KopyCommandLineProcessor.testOption(
        option: AbstractCliOption,
        value: String,
        block: (CompilerConfiguration) -> Unit,
    ) {
        val compilerConfiguration = CompilerConfiguration()
        processOption(option = option, value = value, configuration = compilerConfiguration)
        block(compilerConfiguration)
    }

    private fun KopyCommandLineProcessor.testOption(
        option: AbstractCliOption,
        values: List<String>,
        block: (CompilerConfiguration) -> Unit,
    ) {
        val compilerConfiguration = CompilerConfiguration()
        for (value in values) {
            processOption(option = option, value = value, configuration = compilerConfiguration)
        }
        block(compilerConfiguration)
    }
}
