package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import com.javiersc.kotlin.kopy.compiler.atomicReferenceClassId
import com.javiersc.kotlin.kopy.compiler.underscoreAtomicName
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor

internal class IrAtomicPropertyTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        fun originalProp(): IrStatement = super.visitPropertyNew(declaration)
        if (declaration.name != underscoreAtomicName) return originalProp()
        val atomicReference: IrClassSymbol =
            pluginContext.referenceClass(atomicReferenceClassId) ?: return originalProp()

        val atomicReferenceConstructor: IrConstructor =
            atomicReference.owner.primaryConstructor ?: return originalProp()

        val atomicInitializer: IrExpressionBody =
            pluginContext.declarationIrBuilder(declaration).run {
                val parentClass: IrClass = declaration.parentAsClass
                val typeArg: IrSimpleType = parentClass.defaultType
                val thisReceiver: IrValueParameter = parentClass.thisReceiver!!
                val thisGet: IrGetValue = irGet(thisReceiver)
                val atomicReferenceConstructorCall: IrConstructorCall =
                    irCallConstructor(
                            callee = atomicReferenceConstructor.symbol,
                            typeArguments = listOf(typeArg),
                        )
                        .apply { putValueArgument(index = 0, valueArgument = thisGet) }
                irExprBody(atomicReferenceConstructorCall)
            }
        declaration.backingField?.initializer = atomicInitializer

        return declaration
    }
}
