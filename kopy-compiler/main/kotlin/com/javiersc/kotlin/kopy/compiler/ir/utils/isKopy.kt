package com.javiersc.kotlin.kopy.compiler.ir.utils

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.kopy.KopyFunctionCopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.KopyFunctionUpdateEach
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall

internal val IrElement.isKopyInvoke: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>())

internal val IrElement.isKopyCopy: Boolean
    get() = hasAnnotation(fqName<KopyFunctionCopy>())

internal val IrElement.isKopyCopyOrInvoke: Boolean
    get() = isKopyCopy || isKopyInvoke

internal val IrCall.isKopyInvoke: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>())

internal val IrSimpleFunction.isKopyInvokeOrKopy: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>()) || hasAnnotation(fqName<KopyFunctionCopy>())

internal val IrSimpleFunction.isKopySet: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>())

internal val IrSimpleFunction.isKopyUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrSimpleFunction.isKopyUpdateEach: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdateEach>())

internal val IrCall.isKopySet: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>())

internal val IrCall.isKopyUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrCall.isKopySetOrUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>()) || hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrSimpleFunction.isKopySetOrUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>()) || hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrCall.isKopyUpdateEach: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdateEach>())
