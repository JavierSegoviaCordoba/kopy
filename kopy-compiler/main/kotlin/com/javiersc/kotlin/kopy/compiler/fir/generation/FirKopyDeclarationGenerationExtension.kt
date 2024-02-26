package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.compiler.fir.Key
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.builder.buildEmptyExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class FirKopyDeclarationGenerationExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val kopyableClassId: ClassId = "com.javiersc.kotlin.kopy.runtime.Kopyable".toClassId()
    private val atomicRefClassId: ClassId = "kotlinx.atomicfu.AtomicRef".toClassId()
    private val atomicName = "_atomic".toName()
    private val initKopyableName = "_initKopyable".toName()

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        if (callableId.callableName != atomicName) return emptyList()
        val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()

        val atomicRefType: ConeKotlinType = createAtomicRefType(owner)

        val atomicProperty: FirProperty =
            createMemberProperty(
                owner = context.owner,
                key = Key,
                name = callableId.callableName,
                returnType = atomicRefType,
                config = {
                    status { isOverride = true }
                    modality = Modality.OPEN
                },
            )
        return listOf(atomicProperty.symbol)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        if (callableId.callableName != initKopyableName) return emptyList()
        val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()

        val kopyableType: ConeKotlinType = createKopyableType(owner)

        val initKopyableFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = context.owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = kopyableType,
                    config = {
                        status { isOverride = true }
                        modality = Modality.OPEN
                    },
                )
                .apply { replaceBody(buildEmptyExpressionBlock()) }
        return listOf(initKopyableFunction.symbol)
    }

    private fun createAtomicRefType(owner: FirClassSymbol<*>): ConeKotlinType {
        val atomicRefSymbol: FirRegularClassSymbol =
            session.symbolProvider.getRegularClassSymbolByClassId(atomicRefClassId)!!

        val fromTypeParameterSymbols: List<FirTypeParameterSymbol> =
            atomicRefSymbol.typeParameterSymbols.takeIf(List<FirTypeParameterSymbol>::isNotEmpty)!!
        val toTypes: List<ConeClassLikeType> = listOf(owner.defaultType())

        val substitutor: ConeSubstitutor = session.substitutor(fromTypeParameterSymbols, toTypes)

        val atomicRefType: ConeKotlinType =
            substitutor.substituteOrNull(atomicRefSymbol.defaultType())!!
        return atomicRefType
    }

    private fun createKopyableType(owner: FirClassSymbol<*>): ConeKotlinType {
        val kopyableSymbol: FirRegularClassSymbol =
            session.symbolProvider.getRegularClassSymbolByClassId(kopyableClassId)!!

        val fromTypeParameterSymbols: List<FirTypeParameterSymbol> =
            kopyableSymbol.typeParameterSymbols.takeIf(List<FirTypeParameterSymbol>::isNotEmpty)!!
        val toTypes: List<ConeClassLikeType> = listOf(owner.defaultType())

        val substitutor: ConeSubstitutor = session.substitutor(fromTypeParameterSymbols, toTypes)

        val kopyableType: ConeKotlinType =
            substitutor.substituteOrNull(kopyableSymbol.defaultType())!!
        return kopyableType
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        val hasKopyAnnotation: Boolean =
            classSymbol.annotations.any { it.fqName(session) == fqName<Kopy>() }

        if (!hasKopyAnnotation) return emptySet()

        val names: Set<Name> =
            setOf(
                atomicName,
                initKopyableName,
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
