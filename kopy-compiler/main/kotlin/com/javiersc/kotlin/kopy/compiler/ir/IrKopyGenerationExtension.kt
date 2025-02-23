package com.javiersc.kotlin.kopy.compiler.ir

import com.javiersc.kotlin.kopy.compiler.createReport
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrAtomicPropertyTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrFunctionsTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrSetOrUpdateCallTransformer
import com.javiersc.kotlin.kopy.compiler.ir.transformers.IrUpdateEachCallTransformer
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measurements
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

internal class IrKopyGenerationExtension(private val configuration: CompilerConfiguration) :
    IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.generate(pluginContext)
    }

    @JvmName("generate2")
    private fun IrModuleFragment.generate(pluginContext: IrPluginContext) {
        measureExecution(key = "IR_ATOMIC_PROPERTY_TRANSFORMER") {
            transform(IrAtomicPropertyTransformer(this, pluginContext))
        }
        measureExecution(key = "IR_FUNCTIONS_TRANSFORMER") {
            transform(IrFunctionsTransformer(this, pluginContext))
        }
        measureExecution(key = "IR_UPDATE_EACH_CALL_TRANSFORMER") {
            transform(IrUpdateEachCallTransformer(this, pluginContext))
        }
        measureExecution(key = "IR_SET_OR_UPDATE_CALL_TRANSFORMER") {
            transform(IrSetOrUpdateCallTransformer(this, pluginContext))
        }
        measureExecution(key = "IR_PATCH_DECLARATION_PARENTS") { patchDeclarationParents() }

        measurements.createReport()
    }

    private fun <T> IrModuleFragment.transform(transformer: IrElementTransformer<T?>) {
        this.transform(transformer, null)
    }
}
