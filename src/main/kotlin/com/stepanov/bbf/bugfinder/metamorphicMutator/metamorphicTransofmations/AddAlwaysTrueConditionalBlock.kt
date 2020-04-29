package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.metamorphicMutator.getWhiteSpaceNodesToLines
import java.util.*

class AddAlwaysTrueConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val nodes = file.getWhiteSpaceNodesToLines()
        val maxNum = file.text.lines().size

        repeat(5) {
            val changeLineNum = Random().nextInt(maxNum - 1)

            val trueConditionalBlock = trueConditionalBlock(changeLineNum)

            val newBlockFragment = psiFactory.createBlock(trueConditionalBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()

            val anchor = nodes[changeLineNum]

            if (changeLineNum == 0) {
                return
            }

            checker.addNodeIfPossible(file, anchor, newBlockFragment)
        }
    }

    fun trueConditionalBlock(line : Int) : String {
        val env = getVarEnv(1, line)
        val variables = SynthesizeValidExpression().toSample(env.keys, env.size)

        if (env.size < 1) {
            return ""
        }

        val changedVar = variables!!.random()

        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }

        val result = """    var backup_$changedVar : Int = ${SynthesizeValidExpression().synExpression(env)}
        if (${SynthesizePredicate().synPredicate(env, true, 2)}) {
            backup_$changedVar = $changedVar
            $changedVar = ${SynthesizeValidExpression().synExpression(env)}
            $exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
                println($changedVar)
            }
        }
        $changedVar = backup_$changedVar"""

        return result
    }
}