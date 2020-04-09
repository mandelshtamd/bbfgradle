package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import java.util.*


class AddVariableDeclaration : Transformation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val maxNumOfIns = file.text.lines().size / 2

        repeat(Random().nextInt(maxNumOfIns)) {
            val insLine = Random().nextInt(text.size)
            val generateExpr = generateVarDeclaration()

            text.add(insLine, generateExpr)
            if (!checker.checkTextCompiling(getText(text))) {
                text.removeAt(insLine)
            }
        }

        file = psiFactory.createFile(getText(text))
    }

    fun generateVarDeclaration() : String {
        val numOfPossibleTypes = 5
        val variableName = Random().getRandomVariableName(7)

        val randValue = when (Random().nextInt(numOfPossibleTypes)) {
            0 -> Random().nextInt()
            1 -> Random().nextBoolean()
            2 -> Random().nextDouble()
            3 -> Random().getRandomVariableName(7)
            4 -> Random().nextFloat()
            else -> 0
        }
        return "\tvar $variableName = $randValue"
    }

    private fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")
}