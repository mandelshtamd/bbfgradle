package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.metamorphicMutator.getWhiteSpaceNodesToLines
import com.stepanov.bbf.reduktor.util.getAllChildren
import java.util.*

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val nodes = file.getWhiteSpaceNodesToLines()
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
                file.getAllChildren().firstOrNull { it.text == code }?.delete()
            }
        }
    }

    fun trueGuardBlock(code: String, line: Int): String {
        val res = getVarEnv(1, line)
        return """    if (${SynthesizePredicate().synPredicate(res, true, 2)}) { $code } """
    }
}