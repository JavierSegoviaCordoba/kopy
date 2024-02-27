package com.javiersc.kotlin.kopy.compiler.ir

import com.javiersc.kotlin.kopy.compiler.ir._internal.IrKopyInitFunctionTransformer
import com.javiersc.kotlin.kopy.compiler.ir._internal.IrKopyPropertiesTransformer
import com.javiersc.kotlin.kopy.compiler.ir._internal.IrSetOrUpdateCallTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

internal class IrKopyGenerationExtension(
    private val configuration: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.generate(pluginContext)
    }

    @JvmName("generate2")
    private fun IrModuleFragment.generate(pluginContext: IrPluginContext) {
        transform(IrKopyPropertiesTransformer(this, pluginContext))
        transform(IrKopyInitFunctionTransformer(this, pluginContext))
        transform(IrSetOrUpdateCallTransformer(this, pluginContext))

        println("FINISHED")
    }

    private fun <T> IrModuleFragment.transform(transformer: IrElementTransformer<T?>) {
        this.transform(transformer, null)
    }
}
