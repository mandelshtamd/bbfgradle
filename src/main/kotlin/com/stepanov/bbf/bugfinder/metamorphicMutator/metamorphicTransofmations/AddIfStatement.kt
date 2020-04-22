package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddVariableDeclaration
import com.stepanov.bbf.reduktor.util.getAllChildren
import java.util.*

class AddIfStatement : EquivalentMutation() {
    override fun transform() {
        val lines = file.getAllChildren()
        val maxNumOfIns = lines.size / 2

        repeat(Random().nextInt(maxNumOfIns)) {
            val insIdentifier = Random().nextInt(maxNumOfIns)
            val generateExpr = generateIfExpr()

            val extraNode = psiFactory.createExpression(generateExpr)
            checker.addNodeIfPossible(file, lines[insIdentifier], extraNode, Random().nextBoolean())
        }
    }

    fun generateIfExpr() : String {
        //TODO: make block in if body more complex
        return "if (false) { ${AddVariableDeclaration().generateVarDeclaration()} }"
    }
}