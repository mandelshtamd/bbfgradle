package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiWhiteSpace
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.getAllPSIDFSChildrenOfType
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllChildren
import com.stepanov.bbf.reduktor.util.getAllParentsWithoutThis
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments
import org.jetbrains.kotlin.psi.psiUtil.prevLeaf
import java.util.*

class AddAlwaysFalseConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val maxNum = file.text.lines().size

        for (i in 1..mutationsCount) {
            val changeLineNum = Random().nextInt(maxNum - 1)
            val falseConditionalBlock = falseConditionalBlock(changeLineNum)

            val newBlockFragment = psiFactory.createBlock(falseConditionalBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()
            println(changeLineNum)
            val anchor = getNthWhiteSpace(changeLineNum)
            if (anchor == null) continue

            checker.addNodeIfPossible(file, anchor , newBlockFragment)
        }
    }

    fun falseConditionalBlock(line : Int) : String {
        val env = getVarEnv(1, line)
        var variables = SynthesizeValidExpression().toSample(env, env.size)

        val result = StringBuilder()

        if (variables.isNullOrEmpty()) {
            val randomVarName = Random().getRandomVariableName(5)
            val randomVarValue = Random().nextInt()
            result.append("var $randomVarName = $randomVarValue")
            variables = mutableMapOf(randomVarName to listOf(randomVarValue))
        }

        val modifiedVar = SynthesizeValidExpression().toSample(variables, 1)
        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }


        result.append(
        """$exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
            ${modifiedVar!!.keys.first()} = ${SynthesizeValidExpression().synExpression(variables)}
            println(${modifiedVar.keys.first()})
        }""")

        return result.toString()
    }

    val mutationsCount = 5
}