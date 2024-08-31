package com.javiersc.kotlin.kopy.compiler.ir.utils

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

internal fun IrElement.hasAnnotation(annotation: FqName): Boolean =
    when (this) {
        is IrClass -> hasAnnotation(annotation)
        is IrFunctionAccessExpression -> hasAnnotation(annotation)
        is IrAnnotationContainer -> hasAnnotation(annotation)
        else -> false
    }

internal fun IrFunctionAccessExpression.hasAnnotation(annotation: FqName): Boolean =
    annotations.hasAnnotation(annotation)

internal val IrFunctionAccessExpression.annotations: List<IrConstructorCall>
    get() = symbol.owner.annotations
