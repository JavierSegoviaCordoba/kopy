package com.javiersc.kotlin.kopy.compiler.fir.checker

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.resolve.fqName

internal class FirKopyCheckerExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers: ExpressionCheckers =
        object : ExpressionCheckers() {
            override val callCheckers: Set<FirCallChecker> =
                setOf(
                    object : FirCallChecker() {
                        override fun check(
                            expression: FirCall,
                            context: CheckerContext,
                            reporter: DiagnosticReporter
                        ) {
                            if (
                                expression.annotations.any {
                                    it.fqName(session) == fqName<KopyFunctionInvoke>()
                                }
                            )
                                println()
                        }
                    }
                )
        }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        //        DeclarationPredicate.create {
        //            //            metaAnnotated(fqName<KopyFunction>(), includeItself = true)
        //            //                parentAnnotated(fqName<KopyFunction>())
        //            // annotated(fqName<KopyFunction>())
        //            //                ancestorAnnotated(fqName<KopyFunction>())
        //        }
    }
}
