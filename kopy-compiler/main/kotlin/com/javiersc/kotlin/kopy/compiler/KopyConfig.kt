package com.javiersc.kotlin.kopy.compiler

import org.jetbrains.kotlin.config.CompilerConfiguration

internal class KopyConfig(
    val configuration: CompilerConfiguration,
    val measureStorage: MeasureStorage,
    val onlyDiagnostics: Boolean,
)
