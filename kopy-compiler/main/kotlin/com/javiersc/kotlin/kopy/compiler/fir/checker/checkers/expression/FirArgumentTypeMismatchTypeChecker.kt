@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression

import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError
import com.javiersc.kotlin.kopy.compiler.fir.utils.isKopyFunctionSetCall
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measureKey
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.types.AbstractTypeChecker

internal class FirArgumentTypeMismatchTypeChecker(
    private val session: FirSession,
    private val kopyConfig: KopyConfig,
) : FirCallChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirCall) {
        kopyConfig.measureExecution(key = this::class.measureKey) {
            if (!expression.isKopyFunctionSetCall) return
            if (expression !is FirFunctionCall) return

            val extensionType: ConeKotlinType? = expression.extensionReceiver?.resolvedType
            val argumentType: ConeKotlinType? = expression.arguments.firstOrNull()?.resolvedType

            if (extensionType == null || argumentType == null) return

            val isSubtype: Boolean =
                AbstractTypeChecker.isSubtypeOf(
                    context = session.typeContext,
                    subType = extensionType,
                    superType = argumentType,
                )
            if (!isSubtype) {
                reporter.reportOn(
                    source = expression.arguments.first().source,
                    factory = FirKopyError.ARGUMENT_TYPE_MISMATCH,
                    a = extensionType.renderReadable(),
                    b = argumentType.renderReadable(),
                    positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
                )
            }
        }
    }
}
