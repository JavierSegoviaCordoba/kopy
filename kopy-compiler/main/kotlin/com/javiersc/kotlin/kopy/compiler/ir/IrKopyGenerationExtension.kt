package com.javiersc.kotlin.kopy.compiler.ir

import com.javiersc.kotlin.kopy.compiler.ir._internal.AtomicPropertyTransformer
import com.javiersc.kotlin.kopy.compiler.ir._internal.InitKopyableFunctionTransformer
import com.javiersc.kotlin.kopy.compiler.ir._internal.SetOrUpdateCallTransformer
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
        transform(AtomicPropertyTransformer(this, pluginContext))
        transform(InitKopyableFunctionTransformer(this, pluginContext))
        transform(SetOrUpdateCallTransformer(this, pluginContext))

        println("FINISHED")
    }

    private fun <T> IrModuleFragment.transform(transformer: IrElementTransformer<T?>) {
        this.transform(transformer, null)
    }
}
