package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import kotlin.random.Random

class SynthesizeValidExpression {
    fun synExpression(env: MutableMap<String, List<Int>>): String {
        val worklist = toSample(env.keys, Random.nextInt(env.keys.size))
        if (worklist.isNullOrEmpty())
            return ""

        var unaryMaximumCount = 5

        while (worklist.size > 1) {
            if (Random.nextBoolean() && unaryMaximumCount-- > 0) { // unary expression
                val v = toSample(worklist, 1)
                if (v.isNullOrEmpty())
                    return ""

                val unaryOperationList = unaryOperators.shuffled()
                for (uop in unaryOperationList) {
                    worklist.removeAll(v)
                    worklist.add(composeExpression(uop, "", v.first()))
                    //TODO:add correctness checking
                    break
                }

            } else { // binary expression
                val operands = toSample(worklist, 2)
                if (operands.isNullOrEmpty())
                    return ""

                val binaryOperationList = binaryOperators.shuffled()
                for (bop in binaryOperationList) {
                    worklist.removeAll(operands)
                    worklist.add(composeExpression(bop, operands.elementAt(0), operands.elementAt(1)))
                    //TODO:add correctness checking
                    break
                }
            }
        }
        return worklist.first()
    }

    fun toSample(env: MutableSet<String>, untilNum: Int) : MutableSet<String>? {
        if (env.isEmpty())
            return null
        val sample = mutableSetOf(env.elementAt(0).substringAfter('.'))
        for (num in 1 until untilNum)
            sample.add(env.elementAt(num).substringAfter('.'))
        return sample
    }

    fun composeExpression(operation : String, left_pred : String, right_pred : String) : String{
        if (left_pred == "")
            return "($operation$right_pred)"
        return "($left_pred) $operation ($right_pred)"
    }

    val unaryOperators = listOf("!", "~", "-")
    val binaryOperators = listOf("+", "-", "*", "/", "%", "<<", ">>", "&", "|", "^", ">", ">=", "==", "!=", "<=", "<")
}