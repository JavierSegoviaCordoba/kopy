package com.javiersc.kotlin.kopy.compiler.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

internal fun String.toClassId(): ClassId = FqName(this).run { ClassId(parent(), shortName()) }
