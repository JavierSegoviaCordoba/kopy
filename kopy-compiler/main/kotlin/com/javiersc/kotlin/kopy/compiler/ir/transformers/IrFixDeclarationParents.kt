package com.javiersc.kotlin.kopy.compiler.ir.transformers

import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.patchDeclarationParents

internal class IrFixDeclarationParents(private val moduleFragment: IrModuleFragment) {

    fun patchDeclarationParents() {
        moduleFragment.patchDeclarationParents()
    }
}
