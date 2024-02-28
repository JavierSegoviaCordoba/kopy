package com.javiersc.kotlin.kopy.compiler.ir._internal.utils

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.ir.hasAnnotation
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionKopy
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.runtime.Kopyable
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.classId

internal val IrCall.isKopyInvoke: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>())

internal val IrSimpleFunction.isInitKopyable: Boolean
    get() {
        val hasKopyableReturn = returnType.classOrNull?.owner?.classId == classId<Kopyable<*>>()
        return name == "_initKopyable".toName() && hasKopyableReturn
    }

internal val IrSimpleFunction.isKopyInvokeOrKopy: Boolean
    get() = hasAnnotation(fqName<KopyFunctionInvoke>()) || hasAnnotation(fqName<KopyFunctionKopy>())

internal val IrCall.isKopyUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrCall.isKopySetOrUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>()) || hasAnnotation(fqName<KopyFunctionUpdate>())

internal val IrSimpleFunction.isKopySetOrUpdate: Boolean
    get() = hasAnnotation(fqName<KopyFunctionSet>()) || hasAnnotation(fqName<KopyFunctionUpdate>())