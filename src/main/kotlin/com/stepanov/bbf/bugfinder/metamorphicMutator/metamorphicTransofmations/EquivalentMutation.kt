package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllDFSChildren
import com.stepanov.bbf.reduktor.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFunction
import java.io.File
import javax.script.ScriptEngineManager

abstract class EquivalentMutation : Transformation() {
    protected fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")

    fun getNthWhiteSpace(line : Int) : PsiElement? {
        var curLine = 0
        var prev: PsiWhiteSpace? = null
        for (node in file.getAllDFSChildren()) {
            if (node is PsiWhiteSpace) {
                curLine += node.text.count { it == '\n' }
                if (curLine < line && node.text.contains("\n")) prev = node
            }
        }
        return prev
    }


    companion object {
        fun getVarEnv(beginLine : Int, endLine : Int) : MutableMap<String, List<Int>> {
            val creator = PSICreator("")
            val compiler = JVMCompiler()
            val psi = creator.getPSIForText(file.text)
            val checker = MutationChecker(compiler)
            VariableValuesTracer(psi, creator.ctx!!, checker).trace(beginLine)

            val res = RuntimeVariableValuesCollector(psi, compiler).collect().toMutableMap()

            for (line in beginLine + 1 .. endLine) {
                VariableValuesTracer(psi, creator.ctx!!, checker).trace(line)
                val lineResult = RuntimeVariableValuesCollector(psi, compiler).collect()
                res.putAll(lineResult)
            }

            return res
        }
    }
}