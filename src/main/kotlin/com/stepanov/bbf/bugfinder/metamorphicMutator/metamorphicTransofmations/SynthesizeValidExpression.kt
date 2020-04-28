package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import org.jetbrains.kotlin.resolve.calls.inference.TypeVariable
import javax.script.ScriptEngineManager
import kotlin.random.Random
import kotlin.system.exitProcess

class SynthesizeValidExpression {
    fun synExpression(env: MutableMap<String, List<Int>>): String {
        if (env.isNullOrEmpty()) {
            return String()
        }

        var worklist = toSample(env, Random.nextInt(env.keys.size))

        var unaryMaximumCount = 3
        var atLeastOne = false

        if (worklist.isNullOrEmpty()) {
            return String()
        }

        while (worklist!!.size > 1 || !atLeastOne) {
            if (Random.nextBoolean() && unaryMaximumCount-- > 0 || worklist.size == 1) {
                worklist = generateUnaryExpression(worklist)
            } else {
                worklist = generateBinaryExpression(worklist)
            }

            atLeastOne = true
        }
        return worklist.keys.first()
    }


    fun toSample(env: MutableMap<String, List<Int>>, untilNum: Int) : MutableMap<String, List<Int>>? {
        if (env.isEmpty())
            return null

        val randVar = env.keys.random()
        if (untilNum == 1) {
            return mutableMapOf(randVar.substringAfter('.') to env[randVar].orEmpty())
        }

        val sample = mutableMapOf(randVar.substringAfter('.') to env[randVar].orEmpty())
        for ((variable, value) in env)
            sample[variable.substringAfter('.')] = value
        return sample
    }


    fun composeExpression(operation : String, leftPredicate : String, rightPredicate : String) : String {
        return when(leftPredicate) {
            "" -> "($operation($rightPredicate))"
            else -> "($leftPredicate) $operation ($rightPredicate)"
        }
    }


    fun generateUnaryExpression(worklist : MutableMap<String, List<Int>>) : MutableMap<String, List<Int>>? {
        val variable = toSample(worklist, 1)
        if (variable.isNullOrEmpty()) return null

        val unaryOperator = unaryOperators.random()
        worklist.remove(variable.keys.first())

        val newExpression = composeExpression(unaryOperator, "", variable.keys.first())
        val result = evalExpression(newExpression, variable)

        if (result is Int) worklist.put(newExpression, listOf(result))
        else worklist.putAll(variable)

        return worklist
    }


    fun generateBinaryExpression(worklist : MutableMap<String, List<Int>>?) : MutableMap<String, List<Int>>? {
        if (worklist.isNullOrEmpty()) return null
        val operands = toSample(worklist, 2)

        if (operands.isNullOrEmpty()) return null

        val binaryOperationList = binaryOperators.shuffled()

        for (binaryOperator in binaryOperationList) {
            if (isUndefined(operands, binaryOperator)) {
                continue
            }
            operands.forEach { worklist.remove(it.key) }

            val newExpression = composeExpression(binaryOperator, operands.keys.first(),
                    operands.keys.last())

            val result = evalExpression(newExpression, operands)

            if (result is Int) worklist.put(newExpression, listOf(result))
            else worklist.putAll(operands)

            break
        }
        return worklist
    }


    fun isUndefined(operands : MutableMap<String, List<Int>>, operator : String) : Boolean {
        val rightOperand = operands.keys.last()
        return when (operator) {
            "/" -> return (operands[rightOperand]!!.last() != 0)
            else -> false
        }
    }

    fun evalExpression(expression : String, env : Map<String, List<Int>>) : Int? {
        val sem = ScriptEngineManager()
        val se = sem.getEngineByName("JavaScript")

        var evaledExpression = expression

        for (variable in env.keys) {
            evaledExpression = evaledExpression.replace(variable, env[variable]?.last().toString())
        }

        val result = se.eval(evaledExpression)
        if (result !is Int) return null
        return result
    }

    val unaryOperators = listOf("~", "-")
    val binaryOperators = listOf("+", "-", "*", "/", "%", "<<", ">>", "&", "|", "^")
}