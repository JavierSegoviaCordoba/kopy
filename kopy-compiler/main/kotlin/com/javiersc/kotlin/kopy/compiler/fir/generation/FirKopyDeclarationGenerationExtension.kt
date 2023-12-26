package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.fir.coneKotlinType
import com.javiersc.kotlin.compiler.extensions.fir.createFirAnnotation
import com.javiersc.kotlin.compiler.extensions.fir.toFirTypeParameter
import com.javiersc.kotlin.compiler.extensions.fir.toFirTypeRef
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionKopy
import com.javiersc.kotlin.kopy.compiler.fir.Key
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.utils.isInfix
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getClassDeclaredFunctionSymbols
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class FirKopyDeclarationGenerationExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val kopyableClassId: ClassId = "com.javiersc.kotlin.kopy.runtime.Kopyable".toClassId()
    private val invokeName = "invoke".toName()
    private val copyName = "copy".toName()

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val kopyFunctions: List<FirNamedFunctionSymbol> =
            createKopyInvokeFunctions(callableId, context)

        return kopyFunctions
    }

    private fun createKopyInvokeFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val callableName: Name = callableId.callableName
        if (callableName != invokeName && callableName != copyName) return emptyList()

        val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()

        val invokeFunction: FirSimpleFunction = createInvokeOrCopy(callableId, owner)
        return listOf(invokeFunction.symbol)
    }

    private fun createInvokeOrCopy(
        callableId: CallableId,
        owner: FirClassSymbol<*>
    ): FirSimpleFunction {
        val invokeFunction: FirNamedFunctionSymbol =
            session.symbolProvider
                .getClassDeclaredFunctionSymbols(kopyableClassId, callableId.callableName)
                .first()
        val invokeValueParameter: FirValueParameterSymbol =
            invokeFunction.valueParameterSymbols.first()

        val type: ConeKotlinType =
            owner.kopyableSubstitutor.substituteOrSelf(invokeValueParameter.coneKotlinType)

        val function: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = owner.fir.defaultType(),
                    config = {
                        status {
                            isInfix = invokeFunction.isInfix
                            isOperator = callableId.callableName == invokeName
                        }
                        modality = Modality.FINAL
                        valueParameter(name = copyName, type = type)
                    },
                )
                .apply {
                    val annotationClassId =
                        when (callableId.callableName) {
                            invokeName -> classId<KopyFunctionInvoke>()
                            copyName -> classId<KopyFunctionKopy>()
                            else -> error("This should never happen")
                        }
                    val kopyInvokeTypeRef: FirTypeRef =
                        session.symbolProvider
                            .getRegularClassSymbolByClassId(annotationClassId)
                            ?.toFirTypeRef() ?: return@apply
                    replaceAnnotations(listOf(createFirAnnotation(kopyInvokeTypeRef)))
                    replaceBody(buildBlock())
                }

        return function
    }

    private val kopyableTypeParameterSymbols: List<FirTypeParameterSymbol>
        get() =
            session.symbolProvider
                .getClassLikeSymbolByClassId(kopyableClassId)
                ?.typeParameterSymbols
                .orEmpty()

    private val FirClassSymbol<*>.kopyableSubstitutor: ConeSubstitutor
        get() = session.substitutor(kopyableTypeParameterSymbols, listOf(defaultType()))

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        val names: Set<Name> =
            setOf(
                invokeName,
                copyName,
            )
        return names
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DeclarationPredicate.create { annotated(fqName<Kopy>()) })
    }
}

private fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameterSymbol>,
    toTypeParameters: List<ConeKotlinType>,
): ConeSubstitutor {
    val substitutionMap: Map<FirTypeParameterSymbol, ConeKotlinType> =
        fromTypeParameters.zip(toTypeParameters) { from, to -> from to to }.toMap()

    return substitutorByMap(substitutionMap, this)
}

@JvmName("substitutor1")
private fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameter>,
    toTypeParameters: List<ConeKotlinType>,
): ConeSubstitutor = substitutor(fromTypeParameters.map(FirTypeParameter::symbol), toTypeParameters)

@JvmName("substitutor2")
private fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameterSymbol>,
    toTypeParameters: List<FirTypeParameterSymbol>,
): ConeSubstitutor =
    substitutor(fromTypeParameters, toTypeParameters.map(FirTypeParameterSymbol::coneKotlinType))

@JvmName("substitutor3")
private fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameterRef>,
    toTypeParameters: List<FirTypeParameterSymbol>,
): ConeSubstitutor =
    substitutor(
        fromTypeParameters.map(FirTypeParameterRef::symbol),
        toTypeParameters.map(FirTypeParameterSymbol::coneKotlinType)
    )

@JvmName("substitutor4")
private fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameterRef>,
    toTypeParameters: List<ConeKotlinType>,
): ConeSubstitutor =
    substitutor(fromTypeParameters.map(FirTypeParameterRef::symbol), toTypeParameters)

@JvmName("substitutor5")
private fun FirSession.substitutor(
    fromTypeParameters: Map<ConeTypeProjection, FirBasedSymbol<*>>,
    toTypeParameters: List<ConeKotlinType>,
): ConeSubstitutor {
    val params: List<FirTypeParameterRef> =
        fromTypeParameters.map { (projection: ConeTypeProjection, container: FirBasedSymbol<*>) ->
            projection.toFirTypeParameter(
                session = this,
                key = Key,
                containingDeclarationSymbol = container
            )
        }
    return substitutor(params, toTypeParameters)
}
