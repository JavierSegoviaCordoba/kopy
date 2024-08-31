package com.javiersc.kotlin.kopy.compiler.utils

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName

internal fun String.toCallableId(): CallableId =
    FqName(this).run { CallableId(parent(), shortName()) }
