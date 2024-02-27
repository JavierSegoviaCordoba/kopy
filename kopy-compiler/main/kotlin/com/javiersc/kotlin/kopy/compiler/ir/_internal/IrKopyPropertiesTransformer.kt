package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.ir.createIrBlockBody
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.compiler.extensions.ir.toIrGetValue
import com.javiersc.kotlin.kopy.Kopy
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addSetter
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.SpecialNames

internal class IrKopyPropertiesTransformer(
    private val moduleFragment: IrModuleFragment,
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        fun originalStatement(): IrStatement = super.visitPropertyNew(declaration)

        if (!declaration.parentAsClass.hasAnnotation(fqName<Kopy>())) return originalStatement()

        val property: IrProperty =
            declaration.apply {
                this.isVar = true
                this.modality = Modality.OPEN
                this.backingField?.isFinal = false

                if (this.setter == null) {
                    pluginContext.addSetter(declaration)
                }
            }

        return property
    }

    private fun IrPluginContext.addSetter(property: IrProperty) {
        val field: IrField =
            requireNotNull(property.backingField) {
                "The backing field of the property $property should not be null."
            }
        property
            .addSetter {
                visibility = DescriptorVisibilities.PRIVATE
                returnType = irBuiltIns.unitType
            }
            .apply {
                dispatchReceiverParameter =
                    property.parentAsClass.thisReceiver?.deepCopyWithSymbols(this)
                addValueParameter(SpecialNames.IMPLICIT_SET_PARAMETER, field.type)
                body = createIrBlockBody {
                    val value: IrGetValue =
                        IrGetValueImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            valueParameters[0].type,
                            valueParameters[0].symbol
                        )

                    val setField: IrSetField =
                        IrSetFieldImpl(
                            startOffset = UNDEFINED_OFFSET,
                            endOffset = UNDEFINED_OFFSET,
                            symbol = field.symbol,
                            receiver = dispatchReceiverParameter?.toIrGetValue(),
                            value = value,
                            type = irBuiltIns.unitType
                        )
                    statements.add(setField)
                }
            }
    }
}
