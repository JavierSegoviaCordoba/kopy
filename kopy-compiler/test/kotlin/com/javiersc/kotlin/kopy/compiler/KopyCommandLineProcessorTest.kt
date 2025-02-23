package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyDebug
import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyReportPath
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.stdlib.forth
import com.javiersc.kotlin.stdlib.second
import com.javiersc.kotlin.stdlib.third
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
        val compilerConfiguration = CompilerConfiguration()
        processor.pluginOptions.shouldHaveSize(4)
        processor.pluginOptions.first().optionName shouldBe KopyDebug.NAME
        processor.pluginOptions.second().optionName shouldBe KopyFunctions.NAME
        processor.pluginOptions.third().optionName shouldBe KopyReportPath.NAME
        processor.pluginOptions.forth().optionName shouldBe KopyVisibility.NAME

        processor.processOption(
            option = processor.pluginOptions.first(),
            value = true.toString(),
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Debug] shouldBe true

        processor.processOption(
            option = processor.pluginOptions.first(),
            value = false.toString(),
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Debug] shouldBe false

        processor.processOption(
            option = processor.pluginOptions.second(),
            value = KopyFunctions.All.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Functions] shouldBe KopyFunctions.All.value

        processor.processOption(
            option = processor.pluginOptions.second(),
            value = KopyFunctions.Copy.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Functions] shouldBe KopyFunctions.Copy.value

        processor.processOption(
            option = processor.pluginOptions.second(),
            value = KopyFunctions.Invoke.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Functions] shouldBe KopyFunctions.Invoke.value

        processor.processOption(
            option = processor.pluginOptions.third(),
            value = "build/reports/kopy",
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.ReportPath] shouldBe "build/reports/kopy"

        processor.processOption(
            option = processor.pluginOptions.forth(),
            value = KopyVisibility.Auto.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Visibility] shouldBe KopyVisibility.Auto.value

        processor.processOption(
            option = processor.pluginOptions.forth(),
            value = KopyVisibility.Public.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Visibility] shouldBe KopyVisibility.Public.value

        processor.processOption(
            option = processor.pluginOptions.forth(),
            value = KopyVisibility.Internal.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Visibility] shouldBe KopyVisibility.Internal.value

        processor.processOption(
            option = processor.pluginOptions.forth(),
            value = KopyVisibility.Protected.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Visibility] shouldBe KopyVisibility.Protected.value

        processor.processOption(
            option = processor.pluginOptions.forth(),
            value = KopyVisibility.Private.value,
            configuration = compilerConfiguration,
        )
        compilerConfiguration[KopyKey.Visibility] shouldBe KopyVisibility.Private.value
    }
}
