package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.getAllPSIDFSChildrenOfType
import com.stepanov.bbf.reduktor.parser.PSICreator
import java.util.*

class AddAlwaysFalseConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val nodes = file.getAllPSIDFSChildrenOfType<PsiWhiteSpace>().filter { it.text.contains("\n") }
        val maxNum = file.text.lines().size

        repeat(5) {
            val changeLineNum = Random().nextInt(maxNum - 1)

            val falseConditionalBlock = falseConditionalBlock(changeLineNum)

            val newBlockFragment = psiFactory.createBlock(falseConditionalBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()

            val anchor = nodes[changeLineNum]

            if (changeLineNum == 0) {
                return
            }
            checker.addNodeIfPossible(file, anchor, newBlockFragment)
        }
    }

    fun falseConditionalBlock(line : Int) : String {
        val env = getVarEnv(1, line)
        val variables = SynthesizeValidExpression().toSample(env.keys, env.size)

        if (env.size < 1) {
            return ""
        }

        val modifiedVar = variables!!.random()
        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }

        val result =
        """$exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
            ${modifiedVar} = ${SynthesizeValidExpression().synExpression(env)}
            println($modifiedVar)
        }"""

        return result
    }
}