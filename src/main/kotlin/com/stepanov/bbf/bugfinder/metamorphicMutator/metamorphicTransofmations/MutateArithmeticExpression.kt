package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import org.jetbrains.kotlin.KtNodeTypes
import kotlin.math.abs
import kotlin.random.Random

class MutateArithmeticExpression : Transformation() {
    val mutationsCount = 7

    override fun transform() {
        val operators = file.node.getAllChildrenNodes()
            .filter { it.elementType == KtNodeTypes.INTEGER_CONSTANT }
        operators.forEach {
            val mutatedArithmExpr = doMutation(it.text.toInt())
            replaceArithmExpr(it, mutatedArithmExpr)
        }
    }

    private fun doMutation(source: Int): String {
        var newArithmExpr = ""
        newArithmExpr = when (Random.nextInt() % mutationsCount) {
            0 -> breakdownIntoTerms(source)
//            1 -> "($source or ($source % 2))"
//            2 -> "($source and 0.inv())"
//            3 -> "(($source shl 1) shr 1)"
//            4 -> "($source shl 0)"
//            5 -> "($source + ($source.inv() and $source))"
            else -> generatePrevaluatedExpression(source, Random.nextInt(5))
        }
        return newArithmExpr
    }

    private fun breakdownIntoTerms(source: Int) : String {
        val leftOp = Random.nextInt() % 100000
        var rightOp = 0
        if (source - leftOp < Int.MIN_VALUE) { 
            rightOp = source + leftOp 
            return "(-$leftOp + $rightOp)"
        } else { 
            rightOp = source - leftOp 
            return "($leftOp + $rightOp)"
        }
    }

    private fun replaceArithmExpr(replace: ASTNode, replacement: String) {
        val replacementNode = psiFactory.createArgument(replacement)
        checker.replacePSINodeIfPossible(file, replace.psi, replacementNode)
    }

    private fun generatePrevaluatedExpression(number : Int, termsCount : Int) : String {
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
