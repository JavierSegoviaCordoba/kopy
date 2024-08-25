package com.javiersc.kotlin.kopy.compiler.fir.generation

import com.javiersc.kotlin.compiler.extensions.common.classId
import com.javiersc.kotlin.compiler.extensions.common.fqName
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toName
import com.javiersc.kotlin.compiler.extensions.fir.createFirAnnotation
import com.javiersc.kotlin.compiler.extensions.fir.toFirTypeRef
import com.javiersc.kotlin.kopy.Kopy
import com.javiersc.kotlin.kopy.KopyFunctionCopy
import com.javiersc.kotlin.kopy.KopyFunctionInvoke
import com.javiersc.kotlin.kopy.KopyFunctionSet
import com.javiersc.kotlin.kopy.KopyFunctionUpdate
import com.javiersc.kotlin.kopy.KopyFunctionUpdateEach
import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import com.javiersc.kotlin.kopy.compiler.KopyKey
import com.javiersc.kotlin.kopy.compiler.fir.Key
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
    private val configuration: CompilerConfiguration,
) : FirDeclarationGenerationExtension(session) {

    private val kopyVisibility: KopyVisibility
        get() = configuration.get(KopyKey.Visibility, KopyVisibility.Auto)

    private val kopyFunctions: KopyFunctions
        get() = configuration.get(KopyKey.Functions, KopyFunctions.All)

    private val kopyOptInClassId: ClassId = "com.javiersc.kotlin.kopy.KopyOptIn".toClassId()
    private val kopyFunctionCopyClassId: ClassId = classId<KopyFunctionCopy>()
    private val kopyFunctionInvokeClassId: ClassId = classId<KopyFunctionInvoke>()
    private val kopyFunctionSetClassId: ClassId = classId<KopyFunctionSet>()
    private val kopyFunctionUpdateClassId: ClassId = classId<KopyFunctionUpdate>()
    private val kopyFunctionUpdateEachClassId: ClassId = classId<KopyFunctionUpdateEach>()
    private val atomicRefClassId: ClassId = "kotlinx.atomicfu.AtomicRef".toClassId()
    private val atomicName: Name = "_atomic".toName()
    private val copyName: Name = "copy".toName()
    private val invokeName: Name = "invoke".toName()
    private val setName: Name = "set".toName()
    private val updateName: Name = "update".toName()
    private val updateEachName: Name = "updateEach".toName()

    private val function1Class: FirClassLikeSymbol<*>
        get() = session
            .symbolProvider
            .getClassLikeSymbolByClassId(StandardClassIds.FunctionN(1))!!

    private val function1Type: ConeClassLikeType
        get() = (function1Class.toFirTypeRef().coneType as ConeClassLikeType)
            .withArguments { it.type!! }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DeclarationPredicate.create { annotated(fqName<Kopy>()) })
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        val names: Set<Name> =
            setOf(
                atomicName,
                copyName,
                invokeName,
                setName,
                updateName,
                updateEachName,
            )
        return names
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        if (callableId.callableName != atomicName) return emptyList()
        val owner: FirClassSymbol<*> = context?.owner ?: return emptyList()
        if (!owner.hasAnnotation(classId<Kopy>(), session)) return emptyList()
        if (!owner.isData) return emptyList()

        val atomicRefType: ConeKotlinType = createAtomicRefType(owner)

        val atomicProperty: FirProperty =
            createMemberProperty(
                owner = context.owner,
                key = Key,
                name = callableId.callableName,
                returnType = atomicRefType,
                config = {
                    status { isOverride = false }
                    modality = Modality.FINAL
                    visibility = calculateVisibility(owner)
                },
            )
        return listOf(atomicProperty.symbol)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> = buildList {
        val owner: FirClassSymbol<*> = context?.owner ?: return@buildList
        if (!owner.hasAnnotation(classId<Kopy>(), session)) return@buildList
        if (!owner.isData) return@buildList

        if (kopyFunctions == KopyFunctions.All || kopyFunctions == KopyFunctions.Copy) {
            val copyFunction: FirNamedFunctionSymbol? = createCopyFun(callableId, owner)
            if (copyFunction != null) add(copyFunction)
        }

        if (kopyFunctions == KopyFunctions.All || kopyFunctions == KopyFunctions.Invoke) {
            val invokeFunction: FirNamedFunctionSymbol? = createInvokeFun(callableId, owner)
            if (invokeFunction != null) add(invokeFunction)
        }

        val setFunction: FirNamedFunctionSymbol? = createSetFun(callableId, owner)
        if (setFunction != null) add(setFunction)

        val updateFunction: FirNamedFunctionSymbol? = createUpdateFun(callableId, owner)
        if (updateFunction != null) add(updateFunction)

        val updateEachFunction: FirNamedFunctionSymbol? = createUpdateEachFun(callableId, owner)
        if (updateEachFunction != null) add(updateEachFunction)
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
                returnType = session.builtinTypes.unitType.type,
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
                        ),
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
                returnType = session.builtinTypes.unitType.type,
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
                            val typeParam: ConeTypeParameterType = typeParams.first().toConeType()
                            val copyValueParameterType: ConeKotlinType =
                                session.substitutor(
                                    fromTypeParameters = function1Class.typeParameterSymbols,
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
                        ),
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
            session
                .symbolProvider
                .getClassLikeSymbolByClassId(classId<Iterable<*>>()) ?: return null

        val iterableType: ConeClassLikeType =
            (iterableClass.toFirTypeRef().coneType as ConeClassLikeType)
                .withArguments { it.type!! }

        val updateEachFunction: FirSimpleFunction =
            createMemberFunction(
                owner = owner,
                key = Key,
                name = callableId.callableName,
                returnType = session.builtinTypes.unitType.type,
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
                        session.substitutor(
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
                        ),
                    )
                }
        return updateEachFunction.symbol
    }

    private fun createCopyLikeFunction(
        callableId: CallableId,
        owner: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol? {
        val copyValueParameterType: ConeKotlinType =
            session.substitutor(
                fromTypeParameters = function1Class.typeParameterSymbols,
                toTypeParameters = listOf(
                    owner.defaultType(),
                    session.builtinTypes.unitType.coneType,
                ),
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
                        ),
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
            .symbolProvider
            .getRegularClassSymbolByClassId(classId = classId)
            ?.fir
            ?.symbol
            ?.toFirTypeRef()?.let(::createFirAnnotation)

    private fun calculateVisibility(classSymbol: FirClassSymbol<*>): Visibility {
        val visibility: Visibility =
            classSymbol.primaryConstructorSymbol(session)?.visibility ?: return Visibilities.Public
        val isMoreRestrictive: Boolean = kopyVisibility.isMoreRestrictedThan(visibility)
        return if (isMoreRestrictive) kopyVisibility.toVisibility(visibility)
        else visibility
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

    private val Visibility.restrictive: Int
        get() = when (this) {
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
