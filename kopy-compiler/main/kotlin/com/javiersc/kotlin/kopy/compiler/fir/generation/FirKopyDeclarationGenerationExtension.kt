package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.compiler.fir.Key
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.builder.buildEmptyExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

internal class FirKopyDeclarationGenerationExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    private val kopyInitName = "_kopy_init".toName()

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        if (callableId.callableName != kopyInitName) return emptyList()
        val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()

        val initKopyableFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = owner.defaultType(),
                    config = {
                        status { isOverride = true }
                        modality = Modality.OPEN
                    },
                )
                .apply { replaceBody(buildEmptyExpressionBlock()) }
        return listOf(initKopyableFunction.symbol)
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
                kopyInitName,
            )
        return names
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DeclarationPredicate.create { annotated(fqName<Kopy>()) })
    }
}
