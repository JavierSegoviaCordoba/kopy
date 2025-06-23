package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.KopyFunctionCopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.FirBreakingCallsChecker.CheckerResult.Failure
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.FirBreakingCallsChecker.CheckerResult.Ignore
import com.javiersc.kotlin.kopy.compiler.fir.checker.checkers.expression.FirBreakingCallsChecker.CheckerResult.Success
import com.javiersc.kotlin.kopy.compiler.fir.utils.isKopyFunctionSetOrUpdateOrUpdateEachCall
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measureKey
import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirCallChecker
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirThisOwnerSymbol
import org.jetbrains.kotlin.fir.types.resolvedType

internal class FirBreakingCallsChecker(private val kopyConfig: KopyConfig) :
    FirCallChecker(MppCheckerKind.Common) {

    // TODO: Remove @OptIn(DeprecatedForRemovalCompilerApi::class)
    @OptIn(DeprecatedForRemovalCompilerApi::class)
    override fun check(expression: FirCall, context: CheckerContext, reporter: DiagnosticReporter) =
        kopyConfig.measureExecution(key = this::class.measureKey) {
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

                // is Failure.MissingKopyAnnotation -> {
                //     reporter.reportOn(
                //         source = checkerResult.source,
                //         factory = FirKopyError.MISSING_KOPY_ANNOTATION,
                //         a = checkerResult.element.render(),
                //         context = context,
                //         positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
                //     )
                // }

                is Failure.NoCopyScope -> {
                    reporter.reportOn(
                        source = checkerResult.source,
                        factory = FirKopyError.NO_COPY_SCOPE,
                        a = checkerResult.element.render(),
                        context = context,
                        positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
                    )
                }
            }
        }

    private fun FirCall.isBreakingCallsChain(context: CheckerContext): CheckerResult {
        if (!isKopyFunctionSetOrUpdateOrUpdateEachCall) return Ignore

        val session: FirSession = context.session
        val setOrUpdateCall: FirFunctionCall = asFirOrNull() ?: return Failure.BrokenChain(this)

        val noCopyScopeFailure: Failure? = checkFailureNoCopyScope(context, setOrUpdateCall)
        if (noCopyScopeFailure != null) return noCopyScopeFailure

        val extensionReceiver: FirPropertyAccessExpression =
            setOrUpdateCall.extensionReceiver?.asFirOrNull<FirPropertyAccessExpression>()
                ?: return Failure.BrokenChain(setOrUpdateCall)
        val updateOrSetThisBoundSymbol: FirThisOwnerSymbol<*> =
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

    private fun checkFailureNoCopyScope(
        context: CheckerContext,
        setOrUpdateCall: FirFunctionCall,
    ): Failure.NoCopyScope? {
        val session: FirSession = context.session
        val setOrUpdateCallLambdaAnonymousFunction: FirDeclaration =
            setOrUpdateCall.dispatchReceiver
                ?.asFirOrNull<FirThisReceiverExpression>()
                ?.calleeReference
                ?.boundSymbol
                ?.fir
                ?.asFirOrNull<FirReceiverParameter>()
                ?.containingDeclarationSymbol
                ?.fir ?: return Failure.NoCopyScope(setOrUpdateCall)
        val isCopyScope: Boolean =
            context.callsOrAssignments
                .asSequence()
                .filterIsInstance<FirFunctionCall>()
                .map { it to it.argumentList.arguments }
                .mapNotNull { (functionCall, arguments) ->
                    val symbol: FirFunctionSymbol<*> =
                        functionCall.calleeReference.toResolvedFunctionSymbol()
                            ?: return@mapNotNull null
                    val isKopyInvoke = symbol.hasAnnotation(classId<KopyFunctionInvoke>(), session)
                    val isKopyCopy = symbol.hasAnnotation(classId<KopyFunctionCopy>(), session)
                    if (isKopyInvoke || isKopyCopy) arguments else null
                }
                .flatMap { arguments ->
                    arguments
                        .asSequence()
                        .filterIsInstance<FirAnonymousFunctionExpression>()
                        .map(FirAnonymousFunctionExpression::anonymousFunction)
                }
                .any { it == setOrUpdateCallLambdaAnonymousFunction }

        return if (!isCopyScope) Failure.NoCopyScope(setOrUpdateCall.calleeReference) else null
    }

    private fun FirExpression.isBreakingChainCall(
        session: FirSession,
        updateOrSetThisBoundSymbol: FirThisOwnerSymbol<*>,
    ): CheckerResult {
        val thisBoundSymbol: FirThisOwnerSymbol<*>? =
            this.asFirOrNull<FirThisReceiverExpression>()?.calleeReference?.boundSymbol
        if (updateOrSetThisBoundSymbol == thisBoundSymbol) return Success

        if (this !is FirPropertyAccessExpression) return Failure.BrokenChain(this)

        val isDataClass: Boolean = this.resolvedType.toRegularClassSymbol(session)?.isData == true

        val receiver: FirExpression? = this.dispatchReceiver

        val dispatcherBoundSymbol: FirThisOwnerSymbol<*>? =
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

            // data class MissingKopyAnnotation(override val element: FirElement) : Failure

            data class NoCopyScope(override val element: FirElement) : Failure
        }
    }
}
