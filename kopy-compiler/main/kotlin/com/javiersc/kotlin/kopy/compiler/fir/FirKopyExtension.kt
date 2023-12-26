package com.javiersc.kotlin.kopy.compiler.fir

import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyCheckerExtension
import com.javiersc.kotlin.kopy.compiler.fir.generation.FirKopyDeclarationGenerationExtension
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class FirKopyExtension(
    private val configuration: CompilerConfiguration,
) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        registerCheckers()
        registerGenerators()
    }

    private fun ExtensionRegistrarContext.registerCheckers() {
        +::FirKopyCheckerExtension
    }

    private fun ExtensionRegistrarContext.registerGenerators() {
        +::FirKopyDeclarationGenerationExtension
    }
}

internal object Key : GeneratedDeclarationKey()
