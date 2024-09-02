package com.javiersc.kotlin.kopy.compiler.ir.utils

import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.kopy.compiler.kopyFunctionCopyFqName
import com.javiersc.kotlin.kopy.compiler.kopyFunctionInvokeFqName
import com.javiersc.kotlin.kopy.compiler.kopyFunctionSetFqName
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateEachFqName
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateFqName
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall

internal val IrElement.isKopyCopy: Boolean
    get() = hasAnnotation(kopyFunctionCopyFqName)

internal val IrElement.isKopyInvoke: Boolean
    get() = hasAnnotation(kopyFunctionInvokeFqName)

internal val IrElement.isKopyCopyOrInvoke: Boolean
    get() = isKopyCopy || isKopyInvoke

internal val IrSimpleFunction.isKopySet: Boolean
    get() = hasAnnotation(kopyFunctionSetFqName)

internal val IrSimpleFunction.isKopyUpdate: Boolean
    get() = hasAnnotation(kopyFunctionUpdateFqName)

internal val IrSimpleFunction.isKopyUpdateEach: Boolean
    get() = hasAnnotation(kopyFunctionUpdateEachFqName)

internal val IrCall.isKopySet: Boolean
    get() = hasAnnotation(kopyFunctionSetFqName)

internal val IrCall.isKopyUpdate: Boolean
    get() = hasAnnotation(kopyFunctionUpdateFqName)

internal val IrCall.isKopySetOrUpdate: Boolean
    get() = isKopySet || isKopyUpdate

internal val IrCall.isKopyUpdateEach: Boolean
    get() = hasAnnotation(kopyFunctionUpdateEachFqName)
