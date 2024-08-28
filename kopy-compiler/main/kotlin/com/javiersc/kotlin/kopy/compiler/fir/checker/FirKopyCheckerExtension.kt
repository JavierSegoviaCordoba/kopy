package com.javiersc.kotlin.kopy.compiler.fir.checker

import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.FirKopyDeclarationCheckers
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.FirKopyExpressionCheckers
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class FirKopyCheckerExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers: DeclarationCheckers = FirKopyDeclarationCheckers

    override val expressionCheckers: ExpressionCheckers = FirKopyExpressionCheckers(session)
}
