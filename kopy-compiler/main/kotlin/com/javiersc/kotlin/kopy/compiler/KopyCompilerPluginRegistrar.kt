package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.compiler.fir.FirKopyExtension
import com.javiersc.kotlin.kopy.compiler.ir.IrKopyGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

public class KopyCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        allExtensions(configuration)
    }
}

internal fun ExtensionStorage.allExtensions(
    configuration: CompilerConfiguration,
    additionalConfig: ExtensionStorage.() -> Unit = {},
) {
    FirExtensionRegistrarAdapter.registerExtension(FirKopyExtension(configuration))
    IrGenerationExtension.registerExtension(IrKopyGenerationExtension(configuration))
    additionalConfig()
}
