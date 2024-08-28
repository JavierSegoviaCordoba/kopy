package com.javiersc.kotlin.kopy.compiler.fir.utils

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.KopyFunctionUpdateEach
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
    get() = this == classId<KopyFunctionSet>()

internal val ClassId?.isKopyFunctionUpdateEach: Boolean
    get() = this == classId<KopyFunctionUpdateEach>()

internal val ClassId?.isKopyFunctionSetOrUpdateOrUpdateEach: Boolean
    get() =
        this == classId<KopyFunctionSet>() ||
            this == classId<KopyFunctionUpdate>() ||
            this == classId<KopyFunctionUpdateEach>()
