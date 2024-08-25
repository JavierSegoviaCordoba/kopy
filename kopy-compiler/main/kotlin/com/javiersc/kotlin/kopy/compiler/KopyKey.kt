package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object KopyKey {
    val Visibility = CompilerConfigurationKey<KopyVisibility>(KopyVisibility.NAME)
    val Functions = CompilerConfigurationKey<KopyFunctions>(KopyFunctions.NAME)
}
