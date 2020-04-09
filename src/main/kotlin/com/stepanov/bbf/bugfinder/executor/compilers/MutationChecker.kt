package com.stepanov.bbf.bugfinder.executor.compilers

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeElement
import com.stepanov.bbf.bugfinder.executor.CommonCompiler
import com.stepanov.bbf.bugfinder.executor.CompilationChecker
import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.util.getAllParentsWithoutNode
import org.apache.log4j.Logger
import org.jetbrains.kotlin.psi.KtFile

class MutationChecker(compilers: List<CommonCompiler>, var otherFiles: Project? = null) :
    CompilationChecker(compilers) {

    constructor(compiler: CommonCompiler, otherFiles: Project? = null) : this(listOf(compiler), otherFiles)

    fun replacePSINodeIfPossible(file: PsiFile, node: PsiElement, replacement: PsiElement) =
        replaceNodeIfPossible(file, node.node, replacement.node)

    fun replaceNodeIfPossible(file: PsiFile, node: ASTNode, replacement: ASTNode): Boolean {
        log.debug("Trying to replace $node on $replacement")
        if (node.text.isEmpty() || node == replacement) return checkCompiling(file, otherFiles)
        for (p in node.getAllParentsWithoutNode()) {
            try {
                if (node.treeParent.elementType.index == DUMMY_HOLDER_INDEX) continue
                val oldText = file.text
                val replCopy = replacement.copyElement()
                if ((node as TreeElement).treeParent !== p) {
                    continue
                }
                p.replaceChild(node, replCopy)
                if (oldText == file.text)
                    continue
                if (!checkCompiling(file, otherFiles)) {
                    log.debug("Result = false\nText:\n${file.text}")
                    p.replaceChild(replCopy, node)
                    return false
                } else {
                    log.debug("Result = true\nText:\n${file.text}")
                    return true
                }
            } catch (e: Error) {
            }
        }
        return false
    }

    fun addNodeIfPossible(file: KtFile, anchor: PsiElement, node: PsiElement, before: Boolean = false): Boolean =
        addNodeIfPossible(file as PsiFile, anchor, node, before)

    fun addNodeIfPossible(file: PsiFile, anchor: PsiElement, node: PsiElement, before: Boolean = false): Boolean {
        if (node.text.isEmpty() || node == anchor) return checkCompiling(file, otherFiles)
        return addNodeIfPossibleWithNode(file, anchor, node, before) != null
    }

    fun addNodeIfPossibleWithNode(file: PsiFile, anchor: PsiElement, node: PsiElement, before: Boolean = false): PsiElement? {
        log.debug("Trying to add $node to $anchor")
        if (node.text.isEmpty() || node == anchor) return null
        try {
            val addedNode =
                if (before) anchor.parent.addBefore(node, anchor)
                else anchor.parent.addAfter(node, anchor)
            if (checkCompiling(file, otherFiles)) {
                log.debug("Result = true\nText:\n${file.text}")
                return addedNode
            }
            log.debug("Result = false\nText:\n${file.text}")
            addedNode.parent.node.removeChild(addedNode.node)
            return null
        } catch (e: Throwable) {
            println("e = $e")
            return null
        }
    }

    fun addNodeIfPossible(file: KtFile, anchor: ASTNode, node: ASTNode, before: Boolean = false): Boolean =
        addNodeIfPossible(file, anchor.psi, node.psi, before)

    private val DUMMY_HOLDER_INDEX: Short = 86
    private val log = Logger.getLogger("mutatorLogger")
}