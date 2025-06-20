@file:Suppress("ReturnCount")

package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.fir.createFirAnnotation
import com.javiersc.kotlin.compiler.extensions.fir.toFirTypeRef
import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.KopyKey
import com.javiersc.kotlin.kopy.compiler.atomicReferenceClassId
import com.javiersc.kotlin.kopy.compiler.copyName
import com.javiersc.kotlin.kopy.compiler.fir.Key
import com.javiersc.kotlin.kopy.compiler.invokeName
import com.javiersc.kotlin.kopy.compiler.iterableClassId
import com.javiersc.kotlin.kopy.compiler.kopyClassId
import com.javiersc.kotlin.kopy.compiler.kopyFqName
import com.javiersc.kotlin.kopy.compiler.kopyFunctionCopyClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionInvokeClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionSetClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateClassId
import com.javiersc.kotlin.kopy.compiler.kopyFunctionUpdateEachClassId
import com.javiersc.kotlin.kopy.compiler.kopyOptInClassId
import com.javiersc.kotlin.kopy.compiler.measureExecution
import com.javiersc.kotlin.kopy.compiler.measureKey
import com.javiersc.kotlin.kopy.compiler.serializableAnnotationClassId
import com.javiersc.kotlin.kopy.compiler.setName
import com.javiersc.kotlin.kopy.compiler.transientAnnotationClassId
import com.javiersc.kotlin.kopy.compiler.underscoreAtomicName
import com.javiersc.kotlin.kopy.compiler.updateEachName
import com.javiersc.kotlin.kopy.compiler.updateName
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.primaryConstructorSymbol
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildEmptyExpressionBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeAttributes
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.types.withArguments
import org.jetbrains.kotlin.fir.types.withAttributes
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

internal class FirKopyDeclarationGenerationExtension(
    session: FirSession,
    private val kopyConfig: KopyConfig,
) : FirDeclarationGenerationExtension(session) {

    private val configuration: CompilerConfiguration = kopyConfig.configuration

    private val kopyCopyFunctions: List<KopyCopyFunctions>
        get() = configuration[KopyKey.CopyFunctions, KopyCopyFunctions.entries]

    private val kopyTransformFunctions: List<KopyTransformFunctions>
        get() = configuration[KopyKey.TransformFunctions, KopyTransformFunctions.entries]

    private val kopyVisibility: KopyVisibility
        get() = configuration[KopyKey.Visibility, KopyVisibility.Auto]

    private val function1Class: FirClassLikeSymbol<*>
        get() = session.symbolProvider.getClassLikeSymbolByClassId(StandardClassIds.FunctionN(1))!!

    private val function1Type: ConeClassLikeType
        get() = (function1Class.defaultType()).withArguments { it.type!! }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DeclarationPredicate.create { annotated(kopyFqName) })
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        val names: Set<Name> = buildSet {
            add(underscoreAtomicName)
            addAll(kopyCopyFunctions.map { it.value.toName() })
            addAll(kopyTransformFunctions.map { it.value.toName() })
        }
        return names
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> =
        kopyConfig.measureExecution(key = this::class.measureKey("generateProperties")) {
            if (callableId.callableName != underscoreAtomicName) return emptyList()
            val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()
            if (!owner.hasAnnotation(kopyClassId, session)) return emptyList()
            if (!owner.isData) return emptyList()

            val atomicRefType: ConeKotlinType = createAtomicRefType(owner)

            val atomicVisibility: Visibility =
                calculateVisibility(owner).takeIf {
                    it is Visibilities.Internal || it is Visibilities.Private
                } ?: Visibilities.Internal

            val atomicProperty: FirProperty =
                createMemberProperty(
                        owner = context.owner,
                        key = Key,
                        name = callableId.callableName,
                        returnType = atomicRefType,
                        config = {
                            status { isOverride = false }
                            modality = Modality.FINAL
                            visibility = atomicVisibility
                        },
                    )
                    .apply { addTransientAnnotationIfOwnedBySerializable(owner = owner) }
            return listOf(atomicProperty.symbol)
        }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> =
        kopyConfig.measureExecution(key = this::class.measureKey("generateFunctions")) {
            buildList {
                val owner: FirClassSymbol<*> = context?.owner ?: return@buildList
                if (!owner.hasAnnotation(kopyClassId, session)) return@buildList
                if (!owner.isData) return@buildList

                if (KopyCopyFunctions.Copy in kopyCopyFunctions) {
                    val copyFunction: FirNamedFunctionSymbol? = createCopyFun(callableId, owner)
                    if (copyFunction != null) add(copyFunction)
                }

                if (KopyCopyFunctions.Invoke in kopyCopyFunctions) {
                    val invokeFunction: FirNamedFunctionSymbol? = createInvokeFun(callableId, owner)
                    if (invokeFunction != null) add(invokeFunction)
                }
                if (KopyTransformFunctions.Set in kopyTransformFunctions) {
                    val setFunction: FirNamedFunctionSymbol? = createSetFun(callableId, owner)
                    if (setFunction != null) add(setFunction)
                }

                if (KopyTransformFunctions.Update in kopyTransformFunctions) {
                    val updateFunction: FirNamedFunctionSymbol? = createUpdateFun(callableId, owner)
                    if (updateFunction != null) add(updateFunction)
                }

                if (KopyTransformFunctions.UpdateEach in kopyTransformFunctions) {
                    val updateEachFunction: FirNamedFunctionSymbol? =
                        createUpdateEachFun(callableId, owner)
                    if (updateEachFunction != null) add(updateEachFunction)
                }
            }
        }

    private fun createAtomicRefType(owner: FirClassSymbol<*>): ConeKotlinType {
        val atomicRefSymbol: FirRegularClassSymbol =
            session.getRegularClassSymbolByClassId(atomicReferenceClassId)!!

        val fromTypeParameterSymbols: List<FirTypeParameterSymbol> =
            atomicRefSymbol.typeParameterSymbols.takeIf(List<FirTypeParameterSymbol>::isNotEmpty)!!
        val toTypes: List<ConeClassLikeType> = listOf(owner.defaultType())

        val substitutor: ConeSubstitutor = session.substitutor(fromTypeParameterSymbols, toTypes)

        val atomicRefType: ConeKotlinType =
            substitutor.substituteOrNull(atomicRefSymbol.defaultType())!!
        return atomicRefType
    }

    private fun createCopyFun(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        if (callableId.callableName != copyName) return null
        return createCopyLikeFunction(callableId, owner)
    }

    private fun createInvokeFun(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        if (callableId.callableName != invokeName) return null
        return createCopyLikeFunction(callableId, owner)
    }

    private fun createSetFun(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        if (callableId.callableName != setName) return null
        val setFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = session.builtinTypes.unitType.coneType,
                    config = {
                        status {
                            isOverride = false
                            isInfix = true
                        }
                        modality = Modality.FINAL
                        visibility = calculateVisibility(owner)
                        this.extensionReceiverType { typeParameters ->
                            typeParameters.first().toConeType()
                        }
                        valueParameter(
                            name = "other".toName(),
                            typeProvider = { typeParameters -> typeParameters.first().toConeType() },
                        )
                        typeParameter("S".toName())
                    },
                )
                .apply {
                    replaceBody(buildEmptyExpressionBlock())

                    replaceAnnotations(
                        listOfNotNull(
                            createAnnotation(kopyOptInClassId),
                            createAnnotation(kopyFunctionSetClassId),
                        )
                    )
                }
        return setFunction.symbol
    }

    private fun createUpdateFun(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        if (callableId.callableName != updateName) return null

        val updateFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = session.builtinTypes.unitType.coneType,
                    config = {
                        status {
                            isOverride = false
                            isInfix = true
                        }
                        modality = Modality.FINAL
                        visibility = calculateVisibility(owner)
                        this.extensionReceiverType { typeParameters ->
                            typeParameters.first().toConeType()
                        }
                        typeParameter("U".toName())
                        valueParameter(
                            name = "transform".toName(),
                            typeProvider = { typeParams ->
                                val typeParam: ConeTypeParameterType =
                                    typeParams.first().toConeType()
                                val copyValueParameterType: ConeKotlinType =
                                    session
                                        .substitutor(
                                            fromTypeParameters =
                                                function1Class.typeParameterSymbols,
                                            toTypeParameters = listOf(typeParam, typeParam),
                                        )
                                        .substituteOrSelf(function1Type)
                                copyValueParameterType
                            },
                        )
                    },
                )
                .apply {
                    replaceBody(buildEmptyExpressionBlock())

                    replaceAnnotations(
                        listOfNotNull(
                            createAnnotation(kopyOptInClassId),
                            createAnnotation(kopyFunctionUpdateClassId),
                        )
                    )
                }
        return updateFunction.symbol
    }

    private fun createUpdateEachFun(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        if (callableId.callableName != updateEachName) return null

        val iterableClass: FirClassLikeSymbol<*> =
            session.symbolProvider.getClassLikeSymbolByClassId(iterableClassId) ?: return null

        val iterableType: ConeClassLikeType =
            (iterableClass.toFirTypeRef().coneType as ConeClassLikeType).withArguments { it.type!! }

        val updateEachFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = session.builtinTypes.unitType.coneType,
                    config = {
                        status {
                            isOverride = false
                            isInfix = true
                        }
                        modality = Modality.FINAL
                        visibility = calculateVisibility(owner)
                        this.extensionReceiverType { typeParameters ->
                            val typeParamsAsConeType: List<ConeTypeParameterType> =
                                typeParameters.map { it.toConeType() }
                            session
                                .substitutor(
                                    fromTypeParameters = iterableClass.typeParameterSymbols,
                                    toTypeParameters = typeParamsAsConeType,
                                )
                                .substituteOrSelf(iterableType)
                        }
                        typeParameter("UE".toName())
                        valueParameter(
                            name = "transform".toName(),
                            typeProvider = { typeParamRefs ->
                                function1Type.withArguments { typeParamRefs.first().toConeType() }
                            },
                        )
                    },
                )
                .apply {
                    replaceBody(buildEmptyExpressionBlock())

                    replaceAnnotations(
                        listOfNotNull(
                            createAnnotation(kopyOptInClassId),
                            createAnnotation(kopyFunctionUpdateEachClassId),
                        )
                    )
                }
        return updateEachFunction.symbol
    }

    private fun createCopyLikeFunction(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol {
        val toTypeParameters: List<ConeClassLikeType> =
            listOf(owner.defaultType(), session.builtinTypes.unitType.coneType)
        val copyValueParameterType: ConeKotlinType =
            session
                .substitutor(
                    fromTypeParameters = function1Class.typeParameterSymbols,
                    toTypeParameters = toTypeParameters,
                )
                .substituteOrSelf(function1Type)
                .withAttributes(ConeAttributes.WithExtensionFunctionType)

        val copyFunction: FirSimpleFunction =
            createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = owner.defaultType(),
                    config = {
                        status {
                            isOverride = false
                            isInfix = true
                            isOperator = callableId.callableName == invokeName
                        }
                        modality = Modality.FINAL
                        visibility = calculateVisibility(owner)
                        valueParameter(copyName, copyValueParameterType)
                    },
                )
                .apply {
                    replaceBody(buildEmptyExpressionBlock())

                    replaceAnnotations(
                        listOfNotNull(
                            createAnnotation(kopyOptInClassId),
                            createCopyOrInvokeAnnotation(callableId),
                        )
                    )
                }
        return copyFunction.symbol
    }

    private fun createCopyOrInvokeAnnotation(callableId: CallableId): FirAnnotation? =
        when (callableId.callableName) {
            copyName -> createAnnotation(kopyFunctionCopyClassId)
            invokeName -> createAnnotation(kopyFunctionInvokeClassId)
            else -> null
        }

    private fun createAnnotation(classId: ClassId): FirAnnotation? =
        session
            .getRegularClassSymbolByClassId(classId = classId)
            ?.fir
            ?.symbol
            ?.toFirTypeRef()
            ?.let(::createFirAnnotation)

    private fun FirProperty.addTransientAnnotationIfOwnedBySerializable(owner: FirClassSymbol<*>) {
        val isSerializable: Boolean = owner.hasAnnotation(serializableAnnotationClassId, session)
        if (isSerializable) {
            val replacedAnnotations: List<FirAnnotation> = buildList {
                addAll(annotations)
                createAnnotation(classId = transientAnnotationClassId)?.let(::add)
            }
            replaceAnnotations(replacedAnnotations)
        }
    }

    private fun calculateVisibility(classSymbol: FirClassSymbol<*>): Visibility {
        val visibility: Visibility =
            classSymbol.primaryConstructorSymbol(session)?.visibility ?: return Visibilities.Public
        val isMoreRestrictive: Boolean = kopyVisibility.isMoreRestrictedThan(visibility)
        return if (isMoreRestrictive) kopyVisibility.toVisibility(visibility) else visibility
    }

    private fun KopyVisibility.toVisibility(defaultVisibility: Visibility): Visibility =
        when (this) {
            KopyVisibility.Auto -> defaultVisibility
            KopyVisibility.Public -> Visibilities.Public
            KopyVisibility.Internal -> Visibilities.Internal
            KopyVisibility.Protected -> Visibilities.Protected
            KopyVisibility.Private -> Visibilities.Private
        }

    private fun KopyVisibility.isMoreRestrictedThan(visibility: Visibility): Boolean =
        this.restrictive > visibility.restrictive

    @Suppress("MagicNumber")
    private val Visibility.restrictive: Int
        get() =
            when (this) {
                Visibilities.Public -> 1
                Visibilities.Internal -> 2
                Visibilities.Protected -> 3
                Visibilities.Private -> 4
                else -> 5
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
