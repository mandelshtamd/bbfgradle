package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import org.jetbrains.kotlin.KtNodeTypes
import ru.spbstu.wheels.asBits
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
            4 -> {
                nextValue = getNextNumberOrOperation(number) ?: number
                currentNumber = number or nextValue
                operation = "or"
            }
            5 -> {
                nextValue = getNextNumberAndOperation(number) ?: number
                currentNumber = number and nextValue
                operation = "and"
            }
        }
        return "($currentNumber $operation ${generatePrevaluatedExpression(nextValue, termsCount - 1)})"
    }


    fun getNextNumberOrOperation(number : Int) : Int? {
        val bitArray = Integer.toBinaryString(number)
        var nextNumber = mutableListOf<Char>()

        for (i in 0 until bitArray.length) {
            if (bitArray[i] == '1')
                nextNumber.add(listOf<Char>('0', '1').random())
            else
                nextNumber.add('0')
        }
        println(bitArray)
        println(nextNumber)
        return getIntFromList(nextNumber)
    }


    fun getNextNumberAndOperation(number : Int) : Int? {
        val bitArray = Integer.toBinaryString(number)
        var nextNumber = mutableListOf<Char>()

        for (i in 0 until bitArray.length) {
            if (bitArray[i] == '1')
                nextNumber.add(bitArray[i])
            else
                nextNumber.add(listOf<Char>('0', '1').random())
        }
        return getIntFromList(nextNumber)
    }


    fun getIntFromList(list : List<Char>) : Int {
        var temp = 1
        var result = 0
        for (i in list.size-1 downTo  0) {
            result += temp * (list[i] - '0')
            temp *= 2
        }
        return result
    }


    fun getDenominators(num: Int): MutableList<Int> {
        val result = mutableListOf(1)
        for (i in 2..Math.sqrt(num.toDouble()).toInt()) {
            if (num % i == 0) result.add(i)
        }
        return result
    }
}
