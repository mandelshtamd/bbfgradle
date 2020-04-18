package com.stepanov.bbf.bugfinder.tracer

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.util.getType
import com.stepanov.bbf.reduktor.util.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPropertyParameter
import org.jetbrains.kotlin.resolve.BindingContext

class VariableValuesTracer(private var tree: PsiFile, private val ctx: BindingContext, val checker: MutationChecker) {

    fun trace() {
        val propParams = tree.getAllPSIChildrenOfType<KtParameter>().filter { it.isPropertyParameter() }
        val properties = (tree.getAllPSIChildrenOfType<KtProperty>() + propParams)
            .filter { it.name != null && it.getType(ctx)?.toString()?.contains("Int") ?: false }
        val propsWithPref = properties.map { prop ->
            var prefix = generatePrefix(prop)
            if (prefix.isEmpty()) prefix = "globalVar"
            "$prefix.${prop.name}"
        }
        val propToPrint = properties.mapIndexed { ind, prop -> prop to createPrint(propsWithPref[ind], prop.name!!) }
        for (node in tree.getAllDFSChildren()) {
            if (node is PsiWhiteSpace && node.text.contains("\n") && node.parent !is PsiFile) {
                var bearingNode = node
                //println("parent = ${node.parent.text}")
                val sliced = getSlice(node)
                //println("sliced = ${sliced.map { it.text }}")
                propToPrint
                    .filter {
                        it.first in sliced
                                || it.second.text.contains("globalVar.")
                                || (it.first is KtParameter && generatePrefix(node).contains(generatePrefix(it.first)))
                    }
                    .forEach {
                        val copy = it.second.copy() as KtBlockExpression
                        val res = checker.addNodeIfPossibleWithNode(tree, bearingNode, copy)
                        if (res != null) bearingNode = res
                    }
            }
        }
    }

    fun trace(line: Int) {
        var curLine = 0
        var prev: PsiWhiteSpace? = null
        for (node in tree.getAllDFSChildren()) {
            if (node is PsiWhiteSpace) {
                curLine += node.text.count { it == '\n' }
                if (curLine != line && node.text.contains("\n")) prev = node
            }
            if (node is PsiWhiteSpace && node.text.contains("\n") && node.parent !is PsiFile) {
                if (curLine != line) continue
                var bearingNode = node
                //println("parent = ${node.parent.text}")
                val sliced =
                    if (line != -1) {
                        prev?.let { getSlice(it, node) } ?: getSlice(null, node)
                    } else {
                        getSlice(node)
                    }

                val slicedWithName = sliced.map { el ->
                    val name = when (el) {
                        is KtNamedDeclaration -> el.name ?: ""
                        else -> el.text
                    }
                    var prefix = generatePrefix(el)
                    if (prefix.isEmpty()) prefix = "globalVar"
                    "$prefix.${name}" to name
                }
                slicedWithName
                    .forEach {
                        val printExpr = createPrint(it.first, it.second)
                        val copy = printExpr.copy() as KtBlockExpression
                        val res = checker.addNodeIfPossibleWithNode(tree, bearingNode, copy)
                        if (res != null) bearingNode = res
                    }
            }
        }
    }

    private fun generatePrefix(prop: PsiElement) =
        prop.getAllParentsWithoutThis().reversed()
            .filter { it is KtClassOrObject || it is KtNamedFunction }
            .joinToString(".") {
                when (it) {
                    is KtNamedFunction -> "FUN_${it.name}"
                    is KtClassOrObject -> "CLASS_${it.name}"
                    else -> ""
                }
            }


    private fun createPrint(nameWithPref: String, name: String): PsiElement {
        val fullName = if (!nameWithPref.contains("FUN") && !nameWithPref.contains("globalVar")) "this.$name" else name
        val block = KtPsiFactory(project).createBlock("println(\"VAR_TRACING: $nameWithPref = \$$fullName\")")
        block.lBrace!!.delete()
        block.rBrace!!.delete()
        return block
    }

    private val PsiElement.isPropParam
        get() = this is KtParameter && this.isPropertyParameter()


    private fun getSlice(nodeFrom: PsiElement?, nodeTo: PsiElement): Set<PsiElement> {
        val slice =
            tree.getAllDFSChildren()
                .takeWhile { it != nodeTo }
                .takeLastWhile { nodeFrom?.let { nodeFrom -> it != nodeFrom } ?: true }
        val properties = slice.filterIsInstance<KtValVarKeywordOwner>()
        if (properties.isNotEmpty()) return properties.toSet()
        val binaryExpr =
            slice.filter { it is KtBinaryExpression && it.parent !in slice }.map { it as KtBinaryExpression }
        if (binaryExpr.isNotEmpty()) return binaryExpr.mapNotNull { it.left }
            .filterIsInstance<KtNameReferenceExpression>().toSet()
        return slice.filterIsInstance<KtNameReferenceExpression>().filterElementsWithSameTexts().toSet()
    }


    private fun getSlice(node: PsiElement): Set<PsiElement> {
        val res = mutableSetOf<PsiElement>()
        getPropsUntil(node.parent, node).forEach { res.add(it) }
        node.getAllParentsWithoutThis().zipWithNext().forEach {
            getPropsUntil(it.second, it.first).forEach { res.add(it) }
        }
        return res
    }

    private fun getPropsUntil(node: PsiElement, until: PsiElement) =
        node.getAllChildren()
            .takeWhile { it != until }
            .filter { it is KtProperty || it.isPropParam }


    private val project = tree.project


}