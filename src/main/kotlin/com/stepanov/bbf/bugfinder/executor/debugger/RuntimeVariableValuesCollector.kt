package com.stepanov.bbf.bugfinder.executor.debugger

import com.intellij.psi.PsiFile
import com.stepanov.bbf.bugfinder.executor.CommonCompiler
import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.util.saveOrRemoveToTmp

class RuntimeVariableValuesCollector(private val tree: PsiFile, private val compiler: CommonCompiler) {

    data class IntOrRange(val intVal: Int?, val rangeValue: IntRange?) {
        override fun toString(): String = intVal?.let { "$intVal" } ?: "$rangeValue"
    }

    fun collect(): Map<String, List<IntOrRange>> {
        val path = Project(addMainIfNeed(tree.text)).saveOrRemoveToTmp(true)
        val pathToCompiled = compiler.compile(path)
        if (pathToCompiled.status == -1) return mapOf()
        val res = compiler.exec(pathToCompiled.pathToCompiled)
        val resWithFlattenLists =
            res.split("\n")
                .filter { it.startsWith("VAR_TRACING: ") }
                .map { it.split(" ")[1] to it.split(" = ").last() }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.toMutableSet() }
                .toMutableMap()

        val lists = resWithFlattenLists
            .filter { it.value.first().matches(Regex("""\[.*\]""")) }
            .mapValues { it.value.map { it.split(',').map { it.filter { it != '[' && it != ']' }.trim() } } }

        lists.forEach { resWithFlattenLists.remove(it.key) }


        for (entry in lists) {
            for (list in entry.value) {
                for ((i, num) in list.withIndex()) {
                    resWithFlattenLists.getOrPut("${entry.key}[$i]") { mutableSetOf(num) }.add(num)
                }
            }
        }

        return resWithFlattenLists
            .mapValues { it.value.map { it.toIntOrNull() }.filterNotNull() }
            .filter { it.value.isNotEmpty() }
            .mapValues { it.value.toSet().sortedBy { it }.toMutableList() }
            .mapValues { makeRanges(it.value) }
    }


    private fun makeRanges(list: MutableList<Int>): List<IntOrRange> {
        val res = mutableListOf<IntRange>()
        res.add(list[0]..list[0])
        for (i in 1 until list.size) {
            if (list[i] - res.last().last == 1) {
                res.setLast(res.last().first..list[i])
            } else {
                res.add(list[i]..list[i])
            }
        }
        return res.map { if (it.first == it.last) IntOrRange(it.first, null) else IntOrRange(null, it) }
    }

    private fun <T> MutableList<T>.setLast(value: T) {
        this[this.size - 1] = value
    }

    private fun addMainIfNeed(text: String) =
        if (!text.contains("fun main(")) {
            text + "fun main(args: Array<String>) {\n" +
                    "    println(box())\n" +
                    "}"
        } else text

}