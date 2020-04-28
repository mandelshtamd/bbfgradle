package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllChildren
import com.stepanov.bbf.reduktor.util.getAllDFSChildren
import com.stepanov.bbf.reduktor.util.getAllParentsWithoutThis
import com.stepanov.bbf.reduktor.util.replaceThis
import org.jetbrains.kotlin.BlockExpressionElementType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.*
import java.util.*
import kotlin.random.Random.Default.nextInt

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()

        for (i in 1..mutationsCount) {
            val changeLineNum = Random().nextInt(text.size - 1)

            val anchor = getNthWhiteSpace(changeLineNum)
            if (anchor == null) continue

            val code = text[changeLineNum]

            val trueGuardBlock = trueGuardBlock(code, changeLineNum)
            text[changeLineNum] = trueGuardBlock
            if (!checker.checkTextCompiling(getText(text)))
                text[changeLineNum] = code
        }
        file = psiFactory.createFile(getText(text))
    }

    fun trueGuardBlock(code : String, line : Int) : String {
        val res = getVarEnv(1, line)
        return """    if (${SynthesizePredicate().synPredicate(res, true, 2)}) { 
            ${code}
        }"""
    }

    val mutationsCount = 5
}