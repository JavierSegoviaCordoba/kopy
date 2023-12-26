package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.kopy.compiler.ir._internal.utils.isKopySetOrUpdate
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl

internal class SetOrUpdateFunctionTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        fun originalFunction(): IrStatement = super.visitSimpleFunction(declaration)

        if (!declaration.isKopySetOrUpdate) return originalFunction()

        val function: IrSimpleFunction =
            declaration.apply {
                body =
                    pluginContext.declarationIrBuilder(declaration.symbol).irBlockBody {
                        val unitObjectGet: IrGetObjectValueImpl =
                            pluginContext.irBuiltIns.run { irGetObjectValue(unitType, unitClass) }
                        +irReturn(unitObjectGet)
                    }
            }
        return function
    }
}
