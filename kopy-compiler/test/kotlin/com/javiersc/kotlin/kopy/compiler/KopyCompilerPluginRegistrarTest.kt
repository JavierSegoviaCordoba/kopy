package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.compiler.fir.FirKopyExtension
import com.javiersc.kotlin.kopy.compiler.ir.IrKopyGenerationExtension
import com.javiersc.kotlin.stdlib.second
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.junit.jupiter.api.Test

class KopyCompilerPluginRegistrarTest {

    @Test
    fun `check KopyCompilerPluginRegistrar uses K2`() {
        KopyCompilerPluginRegistrar().supportsK2 shouldBe true
    }

    @Test
    fun `check KopyCompilerPluginRegistrar uses Kopy extensions`() {
        val extensionStorage = CompilerPluginRegistrar.ExtensionStorage()
        val configuration = CompilerConfiguration()
        val kopyCompiler = KopyCompilerPluginRegistrar()
        extensionStorage.run {
            kopyCompiler.run {
                registerExtensions(configuration)
            }
        }
        val extensions: List<Any> = extensionStorage.registeredExtensions.values.flatten()
        extensions.shouldHaveSize(2)
        extensions.first().shouldBeTypeOf<FirKopyExtension>()
        extensions.second().shouldBeTypeOf<IrKopyGenerationExtension>()
    }
}
