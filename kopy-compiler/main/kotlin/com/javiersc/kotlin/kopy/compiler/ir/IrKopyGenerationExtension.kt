package com.javiersc.kotlin.kopy.compiler.ir

import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.name
import com.javiersc.kotlin.kopy.compiler.ir._internal.InvokeCallTransformer
import com.javiersc.kotlin.kopy.compiler.ir._internal.UpdateCallTransformer
import com.javiersc.kotlin.stdlib.tree.TreeNode
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.name.Name

internal class IrKopyGenerationExtension(
    private val configuration: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(InvokeCallTransformer(moduleFragment, pluginContext), null)
        moduleFragment.transform(UpdateCallTransformer(moduleFragment, pluginContext), null)

        println("IrKopyGenerationExtension")
    }

    private fun TreeNode<Name>.addNestedReceivers(call: IrFunctionAccessExpression) {
        val receiver = call.extensionReceiver?.asIrOrNull<IrFunctionAccessExpression>() ?: return
        addChild(TreeNode(receiver.name))
        addNestedReceivers(receiver)
    }
}
