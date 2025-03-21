@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.fir.asFirOrNull
import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.kopyClassId
import com.javiersc.kotlin.kopy.compiler.kopyFqName
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measureKey
import com.javiersc.kotlin.kopy.compiler.setName
import org.jetbrains.kotlin.KtFakeSourceElementKind.AssignmentPluginAltered
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCallOrigin
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.buildUnaryArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.calleeReference
import org.jetbrains.kotlin.fir.expressions.contextArguments
import org.jetbrains.kotlin.fir.expressions.dispatchReceiver
import org.jetbrains.kotlin.fir.expressions.explicitReceiver
import org.jetbrains.kotlin.fir.expressions.extensionReceiver
import org.jetbrains.kotlin.fir.extensions.FirAssignExpressionAltererExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.ClassId

internal class FirKopyAssignExpressionAltererExtension(
    session: FirSession,
    private val kopyConfig: KopyConfig,
) : FirAssignExpressionAltererExtension(session) {

    override fun transformVariableAssignment(
        variableAssignment: FirVariableAssignment
    ): FirStatement? =
        kopyConfig.measureExecution(key = this::class.measureKey) {
            val variableDispatchReceiver: FirQualifiedAccessExpression =
                variableAssignment.dispatchReceiver?.asFirOrNull() ?: return null

            val kopyClassClassId: ClassId =
                variableDispatchReceiver.resolvedType.classId ?: return null
            val kopyClass: FirRegularClassSymbol =
                session.getRegularClassSymbolByClassId(kopyClassClassId) ?: return null

            if (!kopyClass.hasAnnotation(classId = kopyClassId, session = session)) return null

            val leftArgument: FirReference = variableAssignment.calleeReference!!
            val leftSymbol: FirVariableSymbol<*> = leftArgument.toResolvedVariableSymbol()!!
            val leftResolvedType: FirResolvedTypeRef = leftSymbol.resolvedReturnTypeRef
            val rightArgument: FirExpression = variableAssignment.rValue
            val setFunCall: FirStatement = buildFunctionCall {
                source = variableAssignment.source?.fakeElement(AssignmentPluginAltered)
                explicitReceiver = buildPropertyAccessExpression {
                    source = leftArgument.source
                    coneTypeOrNull = leftResolvedType.coneType
                    calleeReference = leftArgument
                    variableAssignment.lValue
                        .asFirOrNull<FirQualifiedAccessExpression>()
                        ?.typeArguments
                        ?.let(typeArguments::addAll)
                    annotations += variableAssignment.annotations
                    explicitReceiver = variableAssignment.explicitReceiver
                    dispatchReceiver = variableAssignment.dispatchReceiver
                    extensionReceiver = variableAssignment.extensionReceiver
                    contextArguments += variableAssignment.contextArguments
                }
                argumentList = buildUnaryArgumentList(rightArgument)
                calleeReference = buildSimpleNamedReference {
                    source = variableAssignment.source
                    name = setName
                }
                origin = FirFunctionCallOrigin.Regular
            }

            return setFunCall
        }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DeclarationPredicate.create { annotated(kopyFqName) })
    }
}
