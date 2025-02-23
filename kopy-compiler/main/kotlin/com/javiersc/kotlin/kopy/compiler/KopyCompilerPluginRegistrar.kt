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
        registerAllExtensions(configuration)
    }
}

internal fun ExtensionStorage.registerAllExtensions(
    configuration: CompilerConfiguration,
    additionalConfig: ExtensionStorage.() -> Unit = {},
) {
    val measureStorage = MeasureStorage()
    val kopyConfig = KopyConfig(configuration, measureStorage)
    registerDisposable(measureStorage)
    FirExtensionRegistrarAdapter.registerExtension(FirKopyExtension(kopyConfig))
    IrGenerationExtension.registerExtension(IrKopyGenerationExtension(kopyConfig))
    additionalConfig()
}
