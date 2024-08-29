package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.compiler.extensions.ir.irType
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.Companion.GET_PROPERTY
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.parentAsClass

internal class IrAtomicFunctionsTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFun() = super.visitSimpleFunction(declaration)
        fun atomicProp(): IrProperty? =
            declaration.parentAsClass.findDeclaration<IrProperty> { it.name == "_atomic".toName() }

        if (declaration.name == "_get_atomic".toName()) {
            val valueProperty: IrProperty =
                atomicProp()
                    ?.irType
                    ?.classOrNull
                    ?.owner
                    ?.findDeclaration<IrProperty> { it.name == "value".toName() }
                    ?: return originalFun()

            declaration.body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                val valueCall: IrFunctionAccessExpression =
                    irCall(
                        valueProperty.getter!!.apply {
                            this@apply.returnType = declaration.parentAsClass.defaultType
                        },
                    ).apply {
                        val atomicPropGetter: IrSimpleFunction =
                            atomicProp()?.getter ?: return originalFun()
                        val atomicGetterCall =
                            pluginContext.declarationIrBuilder(declaration.symbol)
                                .irCall(atomicPropGetter).apply {
                                    this.origin = GET_PROPERTY
//                                    this.dispatchReceiver =
//                                        irGet(declaration.parentAsClass.thisReceiver!!)
                                }
                        origin = GET_PROPERTY
                        dispatchReceiver = atomicGetterCall
                    }
                +irReturn(valueCall)
            }
            return declaration
        }

        if (declaration.name == "_set_atomic".toName()) {
            val lazySetFunction: IrSimpleFunction =
                atomicProp()
                    ?.irType
                    ?.classOrNull
                    ?.owner
                    ?.findDeclaration<IrSimpleFunction> { it.name == "lazySet".toName() }
                    ?: return originalFun()
            declaration.body = pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                +irCall(lazySetFunction).apply {
                    val atomicPropGetter: IrSimpleFunction =
                        atomicProp()?.getter ?: return originalFun()
                    val atomicGetterCall =
                        pluginContext.declarationIrBuilder(declaration.symbol)
                            .irCall(atomicPropGetter).apply {
                                this.origin = GET_PROPERTY
//                                this.dispatchReceiver =
//                                    irGet(declaration.parentAsClass.thisReceiver!!)
                            }

                    dispatchReceiver = atomicGetterCall
                    putValueArgument(0, irGet(declaration.valueParameters[0]))
                }
            }
            return declaration
        }

        return originalFun()
    }
}
