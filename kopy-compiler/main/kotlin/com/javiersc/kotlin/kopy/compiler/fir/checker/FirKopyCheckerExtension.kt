package com.javiersc.kotlin.kopy.compiler.fir.checker

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.compiler.fir.checker.BreakingCallsChecker.CheckerResult.Failure
import com.javiersc.kotlin.kopy.compiler.fir.checker.BreakingCallsChecker.CheckerResult.Ignore
import com.javiersc.kotlin.kopy.compiler.fir.checker.BreakingCallsChecker.CheckerResult.Success
import com.javiersc.kotlin.kopy.compiler.fir.errors.FirKopyError
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DEFAULT
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.getOwnerLookupTag
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.dfa.symbol
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.name.ClassId

internal class FirKopyCheckerExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers: ExpressionCheckers = FirKopyExpressionCheckers
}

private object FirKopyExpressionCheckers : ExpressionCheckers() {
    override val callCheckers: Set<FirCallChecker> =
        setOf(
            BreakingCallsChecker,
        )
}

private object BreakingCallsChecker : FirCallChecker(MppCheckerKind.Common) {

    override fun check(expression: FirCall, context: CheckerContext, reporter: DiagnosticReporter) {
        when (val checkerResult: CheckerResult = expression.isBreakingCallsChain(context)) {
            is Ignore -> return
            is Success -> return
            is Failure.BrokenChain -> {
                reporter.reportOn(
                    source = checkerResult.source,
                    factory = FirKopyError.INVALID_CALL_CHAIN,
                    a = checkerResult.element.render(),
                    context = context,
                    positioningStrategy = DEFAULT,
                )
            }
            is Failure.MissingDataClass -> {
                reporter.reportOn(
                    source = checkerResult.source,
                    factory = FirKopyError.MISSING_DATA_CLASS,
                    a = checkerResult.element.render(),
                    context = context,
                    positioningStrategy = DEFAULT,
                )
            }
            is Failure.MissingKopyAnnotation -> {
                reporter.reportOn(
                    source = checkerResult.source,
                    factory = FirKopyError.MISSING_KOPY_ANNOTATION,
                    a = checkerResult.element.render(),
                    context = context,
                    positioningStrategy = DEFAULT,
                )
            }
        }
    }

    private val FirCall.isKopyFunctionSetOrUpdateCall: Boolean
        get() =
            asFirOrNull<FirFunctionCall>()
                ?.calleeReference
                ?.symbol
                ?.resolvedAnnotationClassIds
                ?.firstOrNull()
                .let { it == classId<KopyFunctionSet>() || it == classId<KopyFunctionUpdate>() }

    private fun FirCall.isBreakingCallsChain(context: CheckerContext): CheckerResult {
        if (!isKopyFunctionSetOrUpdateCall) return Ignore

        val session: FirSession = context.session
        val setOrUpdateCall: FirFunctionCall = asFirOrNull() ?: return Failure.BrokenChain(this)
        val extensionReceiver: FirPropertyAccessExpression =
            setOrUpdateCall.extensionReceiver?.asFirOrNull<FirPropertyAccessExpression>()
                ?: return Failure.BrokenChain(setOrUpdateCall)
        val extensionReceiverOwnerAnnotations: List<ClassId> =
            extensionReceiver.symbol
                ?.getOwnerLookupTag()
                ?.toFirRegularClassSymbol(session)
                ?.resolvedAnnotationClassIds
                .orEmpty()
        if (!extensionReceiverOwnerAnnotations.any { it == classId<Kopy>() }) {
            return Failure.MissingKopyAnnotation(extensionReceiver)
        }
        val updateOrSetThisBoundSymbol: FirBasedSymbol<*> =
            setOrUpdateCall.dispatchReceiver
                ?.asFirOrNull<FirThisReceiverExpression>()
                ?.calleeReference
                ?.boundSymbol ?: return Failure.BrokenChain(setOrUpdateCall)

        val extensionDispatchReceiver: FirExpression =
            extensionReceiver.dispatchReceiver
                ?: return Failure.BrokenChain(extensionReceiver.calleeReference)

        val checkerResult: CheckerResult =
            extensionDispatchReceiver.isBreakingChainCall(session, updateOrSetThisBoundSymbol)
        return checkerResult
    }

    private fun FirExpression.isBreakingChainCall(
        session: FirSession,
        updateOrSetThisBoundSymbol: FirBasedSymbol<*>
    ): CheckerResult {
        val thisBoundSymbol: FirBasedSymbol<*>? =
            this.asFirOrNull<FirThisReceiverExpression>()?.calleeReference?.boundSymbol
        if (updateOrSetThisBoundSymbol == thisBoundSymbol) return Success

        if (this !is FirPropertyAccessExpression) return Failure.BrokenChain(this)

        val isDataClass: Boolean = this.resolvedType.toRegularClassSymbol(session)?.isData ?: false

        val receiver: FirExpression? = this.dispatchReceiver

        val dispatcherBoundSymbol: FirBasedSymbol<*>? =
            receiver?.asFirOrNull<FirThisReceiverExpression>()?.calleeReference?.boundSymbol

        val hasSameBoundSymbol: Boolean = dispatcherBoundSymbol == updateOrSetThisBoundSymbol

        return when {
            hasSameBoundSymbol -> Success
            receiver == null -> Failure.BrokenChain(this)
            receiver !is FirPropertyAccessExpression -> {
                val element = receiver.asFirOrNull<FirResolvable>()?.calleeReference ?: receiver
                Failure.BrokenChain(element)
            }
            !isDataClass -> Failure.MissingDataClass(this.calleeReference)
            else -> receiver.isBreakingChainCall(session, updateOrSetThisBoundSymbol)
        }
    }

    sealed interface CheckerResult {

        data object Ignore : CheckerResult

        data object Success : CheckerResult

        sealed interface Failure : CheckerResult {

            val element: FirElement

            val source: KtSourceElement
                get() = element.source ?: error("No source for $this")

            data class BrokenChain(override val element: FirElement) : Failure

            data class MissingDataClass(override val element: FirElement) : Failure

            data class MissingKopyAnnotation(override val element: FirElement) : Failure
        }
    }
}
