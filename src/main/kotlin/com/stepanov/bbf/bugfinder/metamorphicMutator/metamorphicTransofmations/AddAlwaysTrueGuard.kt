package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.getAllChildrenOfCurLevel
import com.stepanov.bbf.bugfinder.util.getAllPSIDFSChildrenOfType
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments
import java.util.*
import kotlin.random.Random.Default.nextInt

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val nodes = file.getAllPSIDFSChildrenOfType<PsiWhiteSpace>().filter { it.text.contains("\n") }
        val maxNumIterations = file.text.lines().size / 3

        repeat(Random().nextInt(maxNumIterations)) {
            val changeLineNum = Random().nextInt(text.size - 1)


            val trueGuardBlock = trueGuardBlock(text[changeLineNum], changeLineNum)
            val newBlockFragment = psiFactory.createBlock(trueGuardBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()

            val anchor = nodes[changeLineNum]
            checker.addNodeIfPossible(file, anchor, newBlockFragment)
        }
    }

    fun trueGuardBlock(code : String, line : Int) : String {
        val res = getVarEnv(1, line)
        return """    if (${SynthesizePredicate().synPredicate(res, true, 2)}) { $code } """
    }
}