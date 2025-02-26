package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers

import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.declaration.FirDataClassKopyAnnotationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassLikeChecker

internal class FirKopyDeclarationCheckers(kopyConfig: KopyConfig) : DeclarationCheckers() {
    override val classLikeCheckers: Set<FirClassLikeChecker> =
        setOf(FirDataClassKopyAnnotationChecker(kopyConfig))
}
