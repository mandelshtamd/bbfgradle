package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import kotlin.random.Random

class SynthesizePredicate {
    fun synPredicate(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
        if (depth == 0) return synAtomic(env, expected)

        return when(Random.nextInt(4)) {
            0 -> synNegativePredicate(env, expected, depth)
            1 -> synConjunction(env, expected, depth)
            2 -> synDisjunction(env, expected, depth)
            else -> synAtomic(env, expected)
        }
    }

    fun synNegativePredicate(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
        return "!(${synPredicate(env, !expected, depth - 1)})"
    }

    fun synConjunction(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
        val right : Boolean
        val left : Boolean

        if (expected) {
            left = true
            right = true
        } else if (Random.nextBoolean()) {
            left = true
            right = false
        } else {
            left = false
            right = Random.nextBoolean()
        }

        val leftPred = synPredicate(env, left, depth - 1)
        val rightPred = synPredicate(env, right, depth - 1)
        return composeExpression("&&", leftPred, rightPred)
    }

     fun synDisjunction(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
         var left = true
         var right = true

         if (!expected) {
             left = false
             right = false
         } else if (Random.nextBoolean()) {
             left = false
             right = true
         } else {
             left = true
             right = Random.nextBoolean()
         }

         val leftPred = synPredicate(env, left, depth - 1)
         val rightPred = synPredicate(env, left, depth - 1)
         return composeExpression("||", leftPred, rightPred)
     }

    fun composeExpression(operation : String, leftPred : String, rightPred : String) : String{
        if (leftPred == "")
            return "($operation$rightPred)"
        return "($leftPred) $operation ($rightPred)"
    }

     fun synAtomic(env : Map<String, List<Int>>, expected : Boolean) : String {
         if (env.isEmpty())
             return expected.toString()

         val firstRandomVar = env.keys.random()
         val secondRandomVar = env.keys.random()

         val firstVarName = firstRandomVar.substringAfter('.')
         val secondVarName = secondRandomVar.substringAfter('.')

         return when(Random.nextInt(2)) {
             0 -> generateExprWithVarAndConstant(firstVarName, env[firstRandomVar]!!.last(), expected)
             else -> generateExprWithTwoVars(firstVarName, env[firstRandomVar]!!.last(),
                     secondVarName, env[firstRandomVar]!!.last(), expected)
         }
     }

    fun generateExprWithVarAndConstant(variable : String, varArg : Int, expected: Boolean) : String {
        val expr = when(Random.nextInt(2)) {
            0 -> "($variable <= ${varArg + Random.nextInt(Int.MAX_VALUE - varArg)})"
            else -> "($variable < ${varArg + 1 + Random.nextInt(Int.MAX_VALUE - varArg - 1)})"
        }

        return when(expected){
            false -> "(!$expr)"
            else -> expr
        }
    }

    fun generateExprWithTwoVars(firstVar : String, firstVarArg : Int, secondVar : String, secondVarArg : Int, expected: Boolean) : String {
        return when(expected) {
            true && firstVarArg > secondVarArg -> "($firstVar > $secondVar)"
            true && firstVarArg <= secondVarArg -> "($firstVar <= $secondVar)"
            false && firstVarArg > secondVarArg -> "($firstVar <= $secondVar)"
            else -> "($firstVar > $secondVar)"
        }
    }
}