package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyDebug
import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyReportPath
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object KopyKey {
    val Debug = CompilerConfigurationKey<Boolean>(KopyDebug.NAME)
    val Functions = CompilerConfigurationKey<String>(KopyFunctions.NAME)
    val ReportPath = CompilerConfigurationKey<String>(KopyReportPath.NAME)
    val Visibility = CompilerConfigurationKey<String>(KopyVisibility.NAME)
}
