@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.asIrOrNull
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.kopy.compiler.ir.utils.isInitKopyable
import com.javiersc.kotlin.kopy.compiler.ir.utils.isKopyCopy
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.parentAsClass

internal class IrInitKopyableFunctionTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFunction(): IrStatement = super.visitSimpleFunction(declaration)

        if (!declaration.isInitKopyable) return originalFunction()
        if (!declaration.parentAsClass.isData) return originalFunction()

        val function: IrSimpleFunction =
            declaration.apply {
                body =
                    pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                        val copyFun: IrSimpleFunction =
                            declaration.parentAsClass.declarations
                                .filterIsInstance<IrSimpleFunction>()
                                .first {
                                    val element: IrElement = it.asIrOrNull() ?: return@first false
                                    val isKopyCopyFun: Boolean = element.isKopyCopy
                                    it.name == "copy".toName() && !isKopyCopyFun
                                }
                        val copyCall: IrFunctionAccessExpression =
                            irCall(copyFun).apply {
                                dispatchReceiver = irGet(declaration.dispatchReceiverParameter!!)
                            }
                        val irReturn = irReturn(copyCall)
                        +irReturn
                    }
            }
        return function
    }
}
