package com.javiersc.kotlin.kopy.compiler.ir.transformers

import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.declarationIrBuilder
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute

internal class IrAtomicPropertyTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        fun originalProp() = super.visitPropertyNew(declaration)
        if (declaration.name != "_atomic".toName()) return originalProp()

        val atomicRefClassId = "kotlinx.atomicfu.AtomicRef".toClassId()

        val atomicFunctions =
            pluginContext.referenceFunctions("kotlinx.atomicfu.atomic".toCallableId())

        val atomicFunction: IrSimpleFunction =
            atomicFunctions
                .first {
                    val hasOneValueParameter: Boolean = it.owner.valueParameters.count() == 1
                    val hasAtomicRefReturnType: Boolean =
                        it.owner.returnType.classOrNull?.owner?.classId == atomicRefClassId
                    hasOneValueParameter && hasAtomicRefReturnType
                }
                .owner

        val atomicInitializer: IrExpressionBody =
            pluginContext.declarationIrBuilder(declaration).run {
                val atomicCall: IrFunctionAccessExpression =
                    irCall(atomicFunction).apply {
                        val parentClass: IrClass = declaration.parentAsClass
                        val typeArg: IrSimpleType = parentClass.defaultType
                        putTypeArgument(0, typeArg)
                        val thisReceiver: IrValueParameter = parentClass.thisReceiver!!
                        val thisGet: IrGetValue = irGet(thisReceiver)
                        putValueArgument(0, thisGet)

                        val fromType: List<IrTypeParameter> = atomicFunction.typeParameters
                        val callType: IrType = type.substitute(fromType, listOf(typeArg))
                        type = callType
                    }
                irExprBody(atomicCall)
            }
        declaration.backingField?.initializer = atomicInitializer

        return declaration
    }
}
