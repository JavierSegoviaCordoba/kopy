@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers

import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.BreakingCallsChecker
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.ArgumentTypeMismatchTypeChecker
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker

internal class FirKopyExpressionCheckers(
    private val session: FirSession,
) : ExpressionCheckers() {
    override val callCheckers: Set<FirCallChecker> =
        setOf(
            BreakingCallsChecker,
            ArgumentTypeMismatchTypeChecker(session = session),
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
