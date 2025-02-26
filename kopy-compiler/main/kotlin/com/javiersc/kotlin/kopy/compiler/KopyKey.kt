package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyDebug
import com.javiersc.kotlin.kopy.args.KopyReportPath
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object KopyKey {
    val CopyFunctions = key<List<KopyCopyFunctions>>(KopyCopyFunctions.NAME)
    val Debug = key<Boolean>(KopyDebug.NAME)
    val ReportPath = key<String>(KopyReportPath.NAME)
    val TransformFunctions = key<List<KopyTransformFunctions>>(KopyTransformFunctions.NAME)
    val Visibility = key<KopyVisibility>(KopyVisibility.NAME)
}

private fun <T> key(name: String) = CompilerConfigurationKey<T>(name)
