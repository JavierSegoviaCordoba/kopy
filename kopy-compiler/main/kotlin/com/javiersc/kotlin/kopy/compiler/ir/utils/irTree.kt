package com.javiersc.kotlin.kopy.compiler.ir.utils

import com.javiersc.kotlin.compiler.extensions.ir.treeNode
import com.javiersc.kotlin.stdlib.tree.TreeNode
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal val IrModuleFragment.irFilesTree: List<TreeNode<IrElement>>
    get() = files.flatMap(IrFile::treeNode)

internal fun IrElement.findDeclarationParent(moduleFragment: IrModuleFragment): IrElement? {
    val element: TreeNode<IrElement>? = moduleFragment.irFilesTree.firstOrNull { it.value == this }
    return element?.findDeclarationParent()
}

internal fun TreeNode<IrElement>.findDeclarationParent(): IrElement? {
    val parent: TreeNode<IrElement> = parent ?: return null
    return if (parent.value is IrDeclarationParent) parent.value else parent.findDeclarationParent()
}
