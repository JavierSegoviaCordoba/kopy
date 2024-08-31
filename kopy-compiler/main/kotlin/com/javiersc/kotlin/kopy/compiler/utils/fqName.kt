package com.javiersc.kotlin.kopy.compiler.utils

import org.jetbrains.kotlin.name.FqName

internal fun String.toFqName(): FqName = FqName(this)
