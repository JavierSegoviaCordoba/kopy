package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.fir.createFirResolvedTypeRef
import com.javiersc.kotlin.compiler.extensions.fir.toConeType
import com.javiersc.kotlin.kopy.Kopy
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

internal class FirCopySupertypeGenerationExtension(
    session: FirSession,
) : FirSupertypeGenerationExtension(session) {

    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
        typeResolver: TypeResolveService
    ): List<FirResolvedTypeRef> {
        val typeArgument: ConeClassLikeType = classLikeDeclaration.classId.toConeType()
        val type: ConeClassLikeType = classId<Kopyable<*>>().toConeType(typeArgument)
        val supertype: FirResolvedTypeRef = createFirResolvedTypeRef(type)
        return listOf(supertype)
    }

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean =
        declaration.annotations.any { it.isKopy }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(LookupPredicate.create { annotated(fqName<Kopy>()) })
    }

    private val FirAnnotation.isKopy: Boolean
        get() = fqName(session) == fqName<Kopy>()
}
