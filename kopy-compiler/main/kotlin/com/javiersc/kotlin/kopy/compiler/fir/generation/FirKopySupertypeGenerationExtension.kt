package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.fir.createFirResolvedTypeRef
import com.javiersc.kotlin.compiler.extensions.fir.toConeType
import com.javiersc.kotlin.kopy.Kopy as KopyAnnotation
import com.javiersc.kotlin.kopy.runtime.Kopyable
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef

internal class FirKopySupertypeGenerationExtension(
    session: FirSession,
) : FirSupertypeGenerationExtension(session) {

    context(TypeResolveServiceContainer)
    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>
    ): List<FirResolvedTypeRef> {
        if (classLikeDeclaration.annotations.isEmpty()) return emptyList()
        if (classLikeDeclaration.annotations.none { it.isKopy }) return emptyList()
        val typeArgument: ConeClassLikeType = classLikeDeclaration.classId.toConeType()
        val type: ConeClassLikeType = classId<Kopyable<*>>().toConeType(typeArgument)
        val supertype: FirResolvedTypeRef = createFirResolvedTypeRef(type)
        return listOf(supertype)
    }

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean = true

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(LookupPredicate.create { annotated(fqName<KopyAnnotation>()) })
    }

    private val FirAnnotation.isKopy: Boolean
        get() = fqName(session) == fqName<KopyAnnotation>()
}
