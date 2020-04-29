package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import java.util.*

class AddAlwaysTrueConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val maxNum = file.text.lines().size

        for (i in 1..mutationsCount) {
            val changeLineNum = Random().nextInt(maxNum - 1)
            val trueConditionalBlock = trueConditionalBlock(changeLineNum)

            val newBlockFragment = psiFactory.createBlock(trueConditionalBlock)
            newBlockFragment.lBrace?.delete()
            newBlockFragment.rBrace?.delete()

            val anchor = getNthWhiteSpace(changeLineNum)
            if (anchor == null) continue

            checker.addNodeIfPossible(file, anchor, newBlockFragment)
        }
    }

    fun trueConditionalBlock(line : Int) : String {
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

        result.append("""    var backup_$modifiedVar : Int = ${SynthesizeValidExpression().synExpression(env)}
        if (${SynthesizePredicate().synPredicate(env, true, 2)}) {
            backup_$modifiedVar = $modifiedVar
            $modifiedVar = ${SynthesizeValidExpression().synExpression(env)}
            $exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
                println($modifiedVar)
            }
        }
        $modifiedVar = backup_$modifiedVar""")

        return result.toString()
    }

    val mutationsCount = 5
}