package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllChildren
import com.stepanov.bbf.reduktor.util.getAllParentsWithoutThis
import org.jetbrains.kotlin.BlockExpressionElementType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.*
import java.util.*
import kotlin.random.Random.Default.nextInt

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val nodes = file.getAllPSIDFSChildrenOfType<PsiWhiteSpace>().filter { it.text.contains("\n") }
        val maxNumIterations = file.text.lines().size / 3

        for (i in 1..Random().nextInt(maxNumIterations)) {
            val changeLineNum = Random().nextInt(text.size - 1)
            val code = text[changeLineNum].trim(' ')

            if (code.contains("var "))
                continue

            val trueGuardBlock = trueGuardBlock(code, changeLineNum)
            val newBlockFragment = psiFactory.createBlock(trueGuardBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()

            val anchor = nodes[changeLineNum]
            if (changeLineNum == 0) {
                return
            }

            if (checker.addNodeIfPossible(file, anchor, newBlockFragment)) {
                val nodeToDelete = file.getAllChildren().filter { it.text.equals(code) }.firstOrNull()
                if (nodeToDelete != null) nodeToDelete.delete()
            }
        }
    }

    fun trueGuardBlock(code : String, line : Int) : String {
        val res = getVarEnv(1, line)
        return """    if (${SynthesizePredicate().synPredicate(res, true, 2)}) { $code } """
    }
}