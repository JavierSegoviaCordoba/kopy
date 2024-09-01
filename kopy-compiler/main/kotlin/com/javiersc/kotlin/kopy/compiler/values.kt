package com.javiersc.kotlin.kopy.compiler

import com.javiersc.kotlin.kopy.compiler.utils.toCallableId
import com.javiersc.kotlin.kopy.compiler.utils.toClassId
import com.javiersc.kotlin.kopy.compiler.utils.toFqName
import com.javiersc.kotlin.kopy.compiler.utils.toName
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
private const val atomicRef: String = "kotlinx.atomicfu.AtomicRef"
private const val iterable: String = "kotlin.collections.Iterable"
private const val list: String = "kotlin.collections.List"
private const val atomic: String = "kotlinx.atomicfu.atomic"
private const val map: String = "kotlin.collections.map"
private const val also: String = "kotlin.also"

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
internal val atomicRefClassId: ClassId = atomicRef.toClassId()
internal val iterableClassId: ClassId = iterable.toClassId()
internal val listClassId: ClassId = list.toClassId()

internal val underscoreAtomicName: Name = "_atomic".toName()
internal val valueName: Name = "value".toName()
internal val copyName: Name = "copy".toName()
internal val invokeName: Name = "invoke".toName()
internal val setName: Name = "set".toName()
internal val updateName: Name = "update".toName()
internal val updateEachName: Name = "updateEach".toName()

internal val atomicCallableId: CallableId = atomic.toCallableId()
internal val mapCallableId: CallableId = map.toCallableId()
internal val alsoCallableId: CallableId = also.toCallableId()