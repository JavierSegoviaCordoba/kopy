package com.javiersc.kotlin.kopy.compiler.ir._internal

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import org.jetbrains.kotlin.ir.expressions.IrCall

internal val IrCall.isKopyInvoke: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>())

internal val IrCall.isKopyUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdate>())
