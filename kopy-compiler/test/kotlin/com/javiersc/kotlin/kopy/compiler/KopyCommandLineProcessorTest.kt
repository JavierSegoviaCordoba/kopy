package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyVisibility
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
        processor.pluginOptions.shouldHaveSize(1)
        processor.pluginOptions.first().optionName shouldBe KopyVisibility.NAME
    }
}
