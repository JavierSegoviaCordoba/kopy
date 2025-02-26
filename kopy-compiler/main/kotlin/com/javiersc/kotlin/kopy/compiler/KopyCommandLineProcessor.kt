package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyDebug
import com.javiersc.kotlin.kopy.args.KopyReportPath
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Group
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Name
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

public class KopyCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "$Group.$Name"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(
                optionName = KopyDebug.NAME,
                valueDescription = KopyDebug.VALUE_DESCRIPTION,
                description = KopyDebug.DESCRIPTION,
                required = false,
            ),
            CliOption(
                optionName = KopyCopyFunctions.NAME,
                valueDescription = KopyCopyFunctions.VALUE_DESCRIPTION,
                description = KopyCopyFunctions.DESCRIPTION,
                required = false,
                allowMultipleOccurrences = true,
            ),
            CliOption(
                optionName = KopyReportPath.NAME,
                valueDescription = KopyReportPath.VALUE_DESCRIPTION,
                description = KopyReportPath.DESCRIPTION,
                required = false,
            ),
            CliOption(
                optionName = KopyTransformFunctions.NAME,
                valueDescription = KopyTransformFunctions.VALUE_DESCRIPTION,
                description = KopyTransformFunctions.DESCRIPTION,
                required = false,
                allowMultipleOccurrences = true,
            ),
            CliOption(
                optionName = KopyVisibility.NAME,
                valueDescription = KopyVisibility.VALUE_DESCRIPTION,
                description = KopyVisibility.DESCRIPTION,
                required = false,
            ),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        fun <T : Any> put(key: CompilerConfigurationKey<T>, value: T) {
            configuration.put(key, value)
        }

        fun <T : Any> appendList(key: CompilerConfigurationKey<List<T>>, values: List<T>) {
            configuration.appendList(key, values)
        }

        when (option.optionName) {
            KopyDebug.NAME -> {
                val debug: Boolean = value.toBooleanStrictOrNull() ?: false
                put(KopyKey.Debug, debug)
            }
            KopyCopyFunctions.NAME -> {
                val kopyCopyFunctions = KopyCopyFunctions.from(value)
                appendList(KopyKey.CopyFunctions, kopyCopyFunctions)
            }
            KopyReportPath.NAME -> {
                val reportPath: String = value
                put(KopyKey.ReportPath, reportPath)
            }
            KopyTransformFunctions.NAME -> {
                val kopyTransformFunctions = KopyTransformFunctions.from(value)
                appendList(KopyKey.TransformFunctions, kopyTransformFunctions)
            }
            KopyVisibility.NAME -> {
                val kopyVisibility = KopyVisibility.from(value)
                put(KopyKey.Visibility, kopyVisibility)
            }
        }
    }
}
