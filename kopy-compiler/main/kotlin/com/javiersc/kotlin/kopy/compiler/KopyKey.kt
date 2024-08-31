package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object KopyKey {
    val Functions = CompilerConfigurationKey<String>(KopyFunctions.NAME)
    val Visibility = CompilerConfigurationKey<String>(KopyVisibility.NAME)
}
