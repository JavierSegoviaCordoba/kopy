package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.BreakingCallsChecker.CheckerResult.Failure
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.BreakingCallsChecker.CheckerResult.Ignore
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.BreakingCallsChecker.CheckerResult.Success
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol

internal object FirKopyExpressionCheckers : ExpressionCheckers() {

    override val callCheckers: Set<FirCallChecker> =
        setOf(
            BreakingCallsChecker,
        )

    override val functionCallCheckers: Set<FirFunctionCallChecker> =
        setOf(
            ParentClassChecker,
        )
}

private object ParentClassChecker : FirFunctionCallChecker(MppCheckerKind.Common) {

    override fun check(
        expression: FirFunctionCall,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val functionSymbol: FirFunctionSymbol<*> =
            expression.calleeReference.toResolvedFunctionSymbol() ?: return

        val isKopyFunctionSet: Boolean =
            functionSymbol.hasAnnotation(classId<KopyFunctionSet>(), context.session)
        val isKopyFunctionUpdate: Boolean =
            functionSymbol.hasAnnotation(classId<KopyFunctionUpdate>(), context.session)

        val isKopyFunctionSetOrUpdate: Boolean = isKopyFunctionSet || isKopyFunctionUpdate

        if (!isKopyFunctionSetOrUpdate) return

        val extensionReceiver: FirPropertyAccessExpression? =
            expression.extensionReceiver?.asFirOrNull<FirPropertyAccessExpression>()

        val explicitReceiver: FirExpression? = extensionReceiver?.explicitReceiver

        val isExtensionReceiverDispatchReceiverKopyAnnotated: Boolean =
            extensionReceiver
                ?.dispatchReceiver
                ?.resolvedType
                ?.toRegularClassSymbol(session = context.session)
                ?.hasAnnotation(classId<Kopy>(), context.session) == true

        if (explicitReceiver == null && isExtensionReceiverDispatchReceiverKopyAnnotated) return

        val isExtensionReceiverBelongingToKopyAnnotatedClass: Boolean =
            explicitReceiver
                ?.resolvedType
                ?.toRegularClassSymbol(session = context.session)
                ?.hasAnnotation(classId<Kopy>(), context.session) == true

        if (!isExtensionReceiverBelongingToKopyAnnotatedClass) {
            reporter.reportOn(
                source = extensionReceiver?.calleeReference?.source,
                factory = FirKopyError.NO_KOPY_PARENT_CLASS,
                a = expression.calleeReference.name.toString(),
                context = context,
                positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
            )
        }
    }
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
                    positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
                )
            }
            is Failure.MissingDataClass -> {
                reporter.reportOn(
                    source = checkerResult.source,
                    factory = FirKopyError.MISSING_DATA_CLASS,
                    a = checkerResult.element.render(),
                    context = context,
                    positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
                )
            }
            is Failure.MissingKopyAnnotation -> {
                reporter.reportOn(
                    source = checkerResult.source,
                    factory = FirKopyError.MISSING_KOPY_ANNOTATION,
                    a = checkerResult.element.render(),
                    context = context,
                    positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
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
            !isDataClass -> Failure.MissingDataClass(this.calleeReference)
            hasSameBoundSymbol -> Success
            receiver == null -> Failure.BrokenChain(this)
            receiver !is FirPropertyAccessExpression -> {
                val element = receiver.asFirOrNull<FirResolvable>()?.calleeReference ?: receiver
                Failure.BrokenChain(element)
            }
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

/*

val setOrUpdateCallLambdaAnonymousFunction =
    setOrUpdateCall.dispatchReceiver
        .asFir<FirThisReceiverExpression>()
        .calleeReference
        .boundSymbol
        .fir

context.callsOrAssignments
    .asSequence()
    .filterIsInstance<FirFunctionCall>()
    .flatMap { it.argumentList.arguments }
    .filterIsInstance<FirAnonymousFunctionExpression>()
    .firstOrNull { it.anonymousFunction == setOrUpdateCallLambdaAnonymousFunction }

*/
