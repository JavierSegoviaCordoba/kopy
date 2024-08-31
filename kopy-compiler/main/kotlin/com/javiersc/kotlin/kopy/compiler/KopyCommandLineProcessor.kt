package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyFunctions
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
                optionName = KopyFunctions.NAME,
                valueDescription = KopyFunctions.VALUE_DESCRIPTION,
                description = KopyFunctions.DESCRIPTION,
                required = false,
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
        configuration: CompilerConfiguration
    ) {
        fun <T : Any> put(key: CompilerConfigurationKey<T>, value: T) =
            configuration.put(key, value)

        when (option.optionName) {
            KopyFunctions.NAME -> put(KopyKey.Functions, value)
            KopyVisibility.NAME -> put(KopyKey.Visibility, value)
        }
    }
}
