package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Group
import com.javiersc.kotlin.kopy.compiler.KopyCompilerProjectData.Name
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

public class KopyCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "$Group.$Name"

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(
                optionName = KopyVisibility.NAME,
                valueDescription = KopyVisibility.DESCRIPTION,
                description = KopyVisibility.DESCRIPTION,
                required = true,
            ),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        val kopyVisibility: KopyVisibility = KopyVisibility.from(value)
        configuration.put(KopyKey.Visibility, kopyVisibility)
    }
}
