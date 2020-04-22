package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.EquivalentMutation
import java.lang.Math.abs
import kotlin.random.Random

class SynthesizePredicate {
    fun SynPred(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
        if (depth == 0) {
            return SynAtom(env, expected)
        }

        return when(Random.nextInt(4)) {
            0 -> SynNeg(env, expected, depth)
            1 -> SynCon(env, expected, depth)
            2 -> SynDis(env, expected, depth)
            else -> SynAtom(env, expected)
        }
    }

    fun SynNeg(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
        return "!(${SynPred(env, !expected, depth - 1)})"
    }

    fun SynCon(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
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
        val left_pred = SynPred(env, left, depth - 1)
        val right_pred = SynPred(env, right, depth - 1)
        return Expr("&&", left_pred, right_pred)
    }

     fun SynDis(env : Map<String, List<Int>>, expected : Boolean, depth : Int) : String {
         val left : Boolean
         val right : Boolean

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

         val left_pred = SynPred(env, left, depth - 1)
         val right_pred = SynPred(env, left, depth - 1)
         return Expr("||", left_pred, right_pred)
     }

    fun Expr(operation : String, left_pred : String, right_pred : String) : String{
        return "($left_pred) $operation ($right_pred)"
    }

     fun SynAtom(env : Map<String, List<Int>>, expected : Boolean) : String {
         val listOfVars = EquivalentMutation.VarEnvironment().getListOfGlobalVars()
         val mapOfVarsAndValues = EquivalentMutation.VarEnvironment().getMapOfVarsAndValues()
         val firstRandomVar = listOfVars[Random.nextInt(listOfVars.size)]
         val secondRandomVar = listOfVars[Random.nextInt(listOfVars.size)]

         val fv = firstRandomVar.substringAfter('.')
         val sv = secondRandomVar.substringAfter('.')

         when(Random.nextInt(2)) {
             1 -> return generateExprWithVarAndConstant(fv, mapOfVarsAndValues[firstRandomVar]!!.last(), expected)
             else -> return generateExprWithTwoVars(fv, mapOfVarsAndValues[firstRandomVar]!!.last(),
                                                        sv, mapOfVarsAndValues[firstRandomVar]!!.last(), expected)
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