package com.javiersc.kotlin.kopy.compiler.fir

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class FirKopyExtension(
    private val configuration: CompilerConfiguration,
) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        registerCheckers()
        registerGenerators()
    }

    private fun ExtensionRegistrarContext.registerCheckers() {}

    private fun ExtensionRegistrarContext.registerGenerators() {}
}
