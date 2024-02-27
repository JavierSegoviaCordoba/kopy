package com.javiersc.kotlin.kopy.compiler.fir.substitutors

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType

internal fun FirSession.substitutor(
    fromTypeParameters: List<FirTypeParameterSymbol>,
    toTypeParameters: List<ConeKotlinType>,
): ConeSubstitutor {
    val substitutionMap: Map<FirTypeParameterSymbol, ConeKotlinType> =
        fromTypeParameters.zip(toTypeParameters) { from, to -> from to to }.toMap()

    return substitutorByMap(substitutionMap, this)
}
