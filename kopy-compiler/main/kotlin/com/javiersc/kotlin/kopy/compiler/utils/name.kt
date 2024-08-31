package com.javiersc.kotlin.kopy.compiler.utils

import org.jetbrains.kotlin.name.Name

internal fun String.toName(): Name = Name.identifier(this)
