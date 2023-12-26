package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Group
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Name
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

public class KopyCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "$Group.$Name"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            // CliOption(
            //     optionName = ...,
            //     valueDescription = ...,
            //     description = ...,
            //     required = true,
            // ),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        // when (option.optionName) {
        //     ... -> {
        //         configuration.put(..., value)
        //     }
        //     else -> {
        //         error("Unexpected config option: ${option.optionName}")
        //     }
        // }
    }
}
