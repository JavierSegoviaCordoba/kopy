package com.javiersc.kotlin.kopy.compiler.ir

import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.createReport
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrAtomicPropertyTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrFixDeclarationParents
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrFunctionsTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrSetOrUpdateCallTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrUpdateEachCallTransformer
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measureKey
import kotlin.reflect.KClass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrTransformer

internal class IrKopyGenerationExtension(private val kopyConfig: KopyConfig) :
    IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        kopyConfig.measureExecution(key = this@IrKopyGenerationExtension::class.measureKey) {
            moduleFragment.generate(pluginContext)
        }
        kopyConfig.createReport()
    }

    @JvmName("generate2")
    private fun IrModuleFragment.generate(pluginContext: IrPluginContext) {
        measureExecution(IrAtomicPropertyTransformer::class) {
            transform(IrAtomicPropertyTransformer(this, pluginContext))
        }
        measureExecution(IrFunctionsTransformer::class) {
            transform(IrFunctionsTransformer(this, pluginContext))
        }
        measureExecution(IrUpdateEachCallTransformer::class) {
            transform(IrUpdateEachCallTransformer(this, pluginContext))
        }
        measureExecution(IrSetOrUpdateCallTransformer::class) {
            transform(IrSetOrUpdateCallTransformer(this, pluginContext))
        }
        measureExecution(IrFixDeclarationParents::class) {
            IrFixDeclarationParents(this).patchDeclarationParents()
        }
        println()
    }

    private fun <T> IrModuleFragment.transform(transformer: IrTransformer<T?>) {
        this.transform(transformer, null)
    }

    private inline fun IrModuleFragment.measureExecution(kclass: KClass<*>, block: () -> Unit) =
        kopyConfig.measureExecution(key = kclass.measureKey, isNested = true) { block() }
}
