@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers

import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.FirArgumentTypeMismatchTypeChecker
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.FirBreakingCallsChecker
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker

internal class FirKopyExpressionCheckers(private val session: FirSession, kopyConfig: KopyConfig) :
    ExpressionCheckers() {
    override val callCheckers: Set<FirCallChecker> =
        setOf(
            FirBreakingCallsChecker(kopyConfig = kopyConfig),
            FirArgumentTypeMismatchTypeChecker(session = session, kopyConfig = kopyConfig),
        )
    //    override val variableAssignmentCheckers: Set<FirVariableAssignmentChecker> =
    //        setOf(
    //            BreakingVariableAssignmentChecker,
    //        )
}

// private object BreakingVariableAssignmentChecker :
// FirVariableAssignmentChecker(MppCheckerKind.Common) {
//    override fun check(
//        expression: FirVariableAssignment,
//        context: CheckerContext,
//        reporter: DiagnosticReporter
//    ) {
//        TODO("Report assignments issues (same way we report `set` function issues")
//    }
// }
