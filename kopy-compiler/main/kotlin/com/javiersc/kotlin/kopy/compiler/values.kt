package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.compiler.extensions.common.toCallableId
import com.javiersc.kotlin.compiler.extensions.common.toClassId
import com.javiersc.kotlin.compiler.extensions.common.toFqName
import com.javiersc.kotlin.compiler.extensions.common.toName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private const val kopyOptIn: String = "com.javiersc.kotlin.kopy.KopyOptIn"
private const val kopy: String = "com.javiersc.kotlin.kopy.Kopy"
private const val kopyFunctionCopy: String = "com.javiersc.kotlin.kopy.KopyFunctionCopy"
private const val kopyFunctionInvoke: String = "com.javiersc.kotlin.kopy.KopyFunctionInvoke"
private const val kopyFunctionSet: String = "com.javiersc.kotlin.kopy.KopyFunctionSet"
private const val kopyFunctionUpdate: String = "com.javiersc.kotlin.kopy.KopyFunctionUpdate"
private const val kopyFunctionUpdateEach: String = "com.javiersc.kotlin.kopy.KopyFunctionUpdateEach"
private const val atomicReference: String = "kotlin.concurrent.atomics.AtomicReference"
private const val iterable: String = "kotlin.collections.Iterable"
private const val list: String = "kotlin.collections.List"
private const val map: String = "kotlin.collections.map"
private const val also: String = "kotlin.also"
private const val serializableAnnotation: String = "kotlinx.serialization.Serializable"
private const val transientAnnotation: String = "kotlinx.serialization.Transient"

internal val kopyFqName: FqName = kopy.toFqName()
internal val kopyFunctionCopyFqName: FqName = kopyFunctionCopy.toFqName()
internal val kopyFunctionInvokeFqName: FqName = kopyFunctionInvoke.toFqName()
internal val kopyFunctionSetFqName: FqName = kopyFunctionSet.toFqName()
internal val kopyFunctionUpdateFqName: FqName = kopyFunctionUpdate.toFqName()
internal val kopyFunctionUpdateEachFqName: FqName = kopyFunctionUpdateEach.toFqName()

internal val kopyOptInClassId: ClassId = kopyOptIn.toClassId()
internal val kopyClassId: ClassId = kopy.toClassId()
internal val kopyFunctionCopyClassId: ClassId = kopyFunctionCopy.toClassId()
internal val kopyFunctionInvokeClassId: ClassId = kopyFunctionInvoke.toClassId()
internal val kopyFunctionSetClassId: ClassId = kopyFunctionSet.toClassId()
internal val kopyFunctionUpdateClassId: ClassId = kopyFunctionUpdate.toClassId()
internal val kopyFunctionUpdateEachClassId: ClassId = kopyFunctionUpdateEach.toClassId()
internal val atomicReferenceClassId: ClassId = atomicReference.toClassId()
internal val iterableClassId: ClassId = iterable.toClassId()
internal val listClassId: ClassId = list.toClassId()
internal val serializableAnnotationClassId: ClassId = serializableAnnotation.toClassId()
internal val transientAnnotationClassId: ClassId = transientAnnotation.toClassId()

internal val underscoreAtomicName: Name = "_atomic".toName()
internal val loadName: Name = "load".toName()
internal val copyName: Name = "copy".toName()
internal val invokeName: Name = "invoke".toName()
internal val setName: Name = "set".toName()
internal val updateName: Name = "update".toName()
internal val updateEachName: Name = "updateEach".toName()

internal val mapCallableId: CallableId = map.toCallableId()
internal val alsoCallableId: CallableId = also.toCallableId()
