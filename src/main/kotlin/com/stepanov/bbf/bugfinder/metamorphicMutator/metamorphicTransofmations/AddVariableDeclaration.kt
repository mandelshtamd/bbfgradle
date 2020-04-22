package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.util.getAllChildren
import com.stepanov.bbf.reduktor.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.KtNodeType
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.stubs.elements.KtObjectElementType
import java.util.*
import kotlin.random.Random.Default.nextInt


class AddVariableDeclaration : EquivalentMutation() {
    override fun transform() {
        val lines = file.getAllChildren()
        val maxNumOfIns = lines.size / 2

        repeat(Random().nextInt(maxNumOfIns)) {
            val insIdentifier = Random().nextInt(maxNumOfIns)
            val generateExpr = generateVarDeclaration()

            val extraNode = psiFactory.createProperty(generateExpr)
            checker.addNodeIfPossible(file, lines[insIdentifier], extraNode, Random().nextBoolean())
        }
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
        return "val $variableName = $randValue"
    }
}