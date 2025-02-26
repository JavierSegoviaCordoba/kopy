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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.junit.jupiter.api.Test

class KopyCommandLineProcessorTest {

    @Test
    fun `check plugin id`() {
        val processor = KopyCommandLineProcessor()
        processor.pluginId shouldBe "com.javiersc.kotlin.kopy-compiler"
    }

    @Test
    fun `check plugin options`() {
        val processor = KopyCommandLineProcessor()
        val pluginOptions: Collection<AbstractCliOption> = processor.pluginOptions
        pluginOptions.shouldHaveSize(5)
        pluginOptions.first().optionName shouldBe KopyDebug.NAME
        pluginOptions.second().optionName shouldBe KopyCopyFunctions.NAME
        pluginOptions.third().optionName shouldBe KopyReportPath.NAME
        pluginOptions.forth().optionName shouldBe KopyTransformFunctions.NAME
        pluginOptions.fifth().optionName shouldBe KopyVisibility.NAME

        processor.testOption(option = pluginOptions.first(), value = "${true}") { configuration ->
            configuration[KopyKey.Debug] shouldBe true
        }

        processor.testOption(option = pluginOptions.first(), value = "${false}") { configuration ->
            configuration[KopyKey.Debug] shouldBe false
        }

        processor.testOption(
            option = pluginOptions.second(),
            values = KopyCopyFunctions.entries.map(KopyCopyFunctions::value),
        ) { configuration ->
            configuration[KopyKey.CopyFunctions].shouldContainExactly(KopyCopyFunctions.entries)
        }

        processor.testOption(
            option = pluginOptions.second(),
            value = KopyCopyFunctions.Copy.value,
        ) { configuration ->
            configuration[KopyKey.CopyFunctions].shouldContainExactly(KopyCopyFunctions.Copy)
        }

        processor.testOption(
            option = pluginOptions.second(),
            value = KopyCopyFunctions.Invoke.value,
        ) { configuration ->
            configuration[KopyKey.CopyFunctions].shouldContainExactly(KopyCopyFunctions.Invoke)
        }

        processor.testOption(option = pluginOptions.third(), value = "build/reports/kopy") {
            configuration ->
            configuration[KopyKey.ReportPath] shouldBe "build/reports/kopy"
        }

        processor.testOption(
            option = pluginOptions.forth(),
            value = KopyTransformFunctions.Set.value,
        ) { configuration ->
            configuration[TransformFunctions].shouldContainExactly(KopyTransformFunctions.Set)
        }

        processor.testOption(
            option = pluginOptions.forth(),
            values = KopyTransformFunctions.entries.map(KopyTransformFunctions::value),
        ) { configuration ->
            configuration[TransformFunctions].shouldContainExactly(
                KopyTransformFunctions.Set,
                KopyTransformFunctions.Update,
                KopyTransformFunctions.UpdateEach,
            )
        }

        processor.testOption(option = pluginOptions.fifth(), value = KopyVisibility.Auto.value) {
            configuration ->
            configuration[KopyKey.Visibility] shouldBe KopyVisibility.Auto
        }

        processor.testOption(option = pluginOptions.fifth(), value = KopyVisibility.Public.value) {
            configuration ->
            configuration[KopyKey.Visibility] shouldBe KopyVisibility.Public
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Internal.value,
        ) { configuration ->
            configuration[KopyKey.Visibility] shouldBe KopyVisibility.Internal
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Protected.value,
        ) { configuration ->
            configuration[KopyKey.Visibility] shouldBe KopyVisibility.Protected
        }

        processor.testOption(
            option = pluginOptions.fifth(),
            value = KopyVisibility.Private.value,
        ) { configuration ->
            configuration[KopyKey.Visibility] shouldBe KopyVisibility.Private
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
