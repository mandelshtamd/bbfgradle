package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import org.jetbrains.kotlin.KtNodeTypes
import kotlin.math.abs
import kotlin.random.Random

class MutateArithmeticExpression : Transformation() {
    val mutationsCount = 3

    override fun transform() {
        val operators = file.node.getAllChildrenNodes()
            .filter { it.elementType == KtNodeTypes.INTEGER_CONSTANT }

        operators.forEach {
            if (it.text.last() == 'L')
                return@forEach

            val mutatedArithmExpr = doMutation(it.text.toInt())
            replaceArithmExpr(it, mutatedArithmExpr)
        }
    }

    private fun doMutation(source: Int): String {
        return generatePrevaluatedExpression(source, Random.nextInt(5))
    }


    private fun replaceArithmExpr(replace: ASTNode, replacement: String) {
        val replacementNode = psiFactory.createArgument(replacement)
        checker.replacePSINodeIfPossible(file, replace.psi, replacementNode)
    }


    fun generatePrevaluatedExpression(number : Int, termsCount : Int) : String {
        if (number == 0)
            return 0.toString()

        var currentNumber = number
        var nextValue = 0
        var operation = ""
        val denominators = getDenominators(number)

        if (termsCount == 0)
            return number.toString()

        when(Random.nextInt(4)) {
            0 -> {
                nextValue = Random.nextInt(number)
                currentNumber -= nextValue
                operation = "+"
            }
            1 -> {
                nextValue = Random.nextInt(number)
                currentNumber += nextValue
                operation = "-"
            }
            2 -> {
                nextValue = 1 + Random.nextInt(1000000)
                currentNumber = number * nextValue
                operation = "/"
            }
            3 -> {
                nextValue = denominators[Random.nextInt(denominators.size)]
                currentNumber = number / nextValue
                operation = "*"
            }
        }

        return "($currentNumber $operation ${generatePrevaluatedExpression(nextValue, termsCount - 1)})"
    }


    fun getDenominators(num: Int): MutableList<Int> {
        val result = mutableListOf(1)
        for (i in 2..Math.sqrt(num.toDouble()).toInt()) {
            if (num % i == 0) result.add(i)
        }
        return result
    }
}
