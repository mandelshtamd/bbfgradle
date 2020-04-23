package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import kotlin.random.Random

class SynthesizeValidExpression {
    fun synExpression(env: MutableMap<String, List<Int>>): String {
        val worklist = toSample(env.keys, Random.nextInt(env.keys.size))
        if (worklist.isNullOrEmpty())
            return ""

        var unaryMaximumCount = 5
        var atLeastOne = false

        while (worklist.size > 1 || !atLeastOne) {
            if (Random.nextBoolean() && unaryMaximumCount-- > 0 || worklist.size == 1) { // unary expression
                val v = toSample(worklist, 1)
                if (v.isNullOrEmpty())
                    return ""

                val unaryOperationList = unaryOperators.shuffled()
                for (uop in unaryOperationList.shuffled()) {
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
                    worklist.add(composeExpression(bop, operands.random(), operands.random()))
                    //TODO:add correctness checking
                    break
                }
            }
            atLeastOne = true
        }
        return worklist.first()
    }

    fun toSample(env: MutableSet<String>, untilNum: Int) : MutableSet<String>? {
        if (env.isEmpty())
            return null

        if (untilNum == 1) return mutableSetOf(env.random().substringAfter('.'))

        val sample = mutableSetOf(env.random().substringAfter('.'))
        for (num in 0 until untilNum)
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