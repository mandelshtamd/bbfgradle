package com.stepanov.bbf.bugfinder.metamorphicMutator

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllPSIDFSChildrenOfType
import org.jetbrains.kotlin.psi.KtPsiFactory

fun PsiFile.getWhiteSpaceNodesToLines() =
    this.getAllPSIDFSChildrenOfType<PsiWhiteSpace>()
        .filter { it.text.contains("\n") }
        .flatMap { node -> MutableList(node.text.count { it == '\n' }) { node } }
