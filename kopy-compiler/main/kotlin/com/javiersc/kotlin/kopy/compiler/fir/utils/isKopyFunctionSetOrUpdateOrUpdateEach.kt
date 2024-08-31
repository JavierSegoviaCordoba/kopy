package com.javiersc.kotlin.kopy.compiler.fir.utils


import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.compiler.kopyFunctionSetClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateEachClassId
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.name.ClassId

internal val FirCall.isKopyFunctionSetCall: Boolean
    get() {
        val annotations =
            asFirOrNull<FirFunctionCall>()
                ?.calleeReference
                ?.symbol
                ?.resolvedAnnotationClassIds
        return annotations?.any { it.isKopyFunctionSet == true } == true
    }

internal val FirCall.isKopyFunctionUpdateEachCall: Boolean
    get() {
        val annotations =
            asFirOrNull<FirFunctionCall>()
                ?.calleeReference
                ?.symbol
                ?.resolvedAnnotationClassIds
        return annotations?.any { it.isKopyFunctionUpdateEach == true } == true
    }

internal val FirCall.isKopyFunctionSetOrUpdateOrUpdateEachCall: Boolean
    get() {
        val annotations =
            asFirOrNull<FirFunctionCall>()
                ?.calleeReference
                ?.symbol
                ?.resolvedAnnotationClassIds
        return annotations?.any { it.isKopyFunctionSetOrUpdateOrUpdateEach == true } == true
    }

internal val ClassId?.isKopyFunctionSet: Boolean
    get() = this == kopyFunctionSetClassId

internal val ClassId?.isKopyFunctionUpdate: Boolean
    get() = this == kopyFunctionUpdateClassId

internal val ClassId?.isKopyFunctionUpdateEach: Boolean
    get() = this == kopyFunctionUpdateEachClassId

internal val ClassId?.isKopyFunctionSetOrUpdateOrUpdateEach: Boolean
    get() = isKopyFunctionSet || isKopyFunctionUpdate || isKopyFunctionUpdateEach
