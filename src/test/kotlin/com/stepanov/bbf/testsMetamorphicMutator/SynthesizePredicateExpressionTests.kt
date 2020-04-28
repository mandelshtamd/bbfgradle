package com.stepanov.bbf.testsMetamorphicMutator

import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.SynthesizePredicate
import org.jetbrains.kotlin.resolve.constants.evaluate.parseBoolean
import org.junit.Test
import javax.script.ScriptEngineManager
import kotlin.test.assertEquals

class SynthesizePredicateExpressionTests {
    @Test
    fun testSynthesizingTrueExpressionSingleValue() {
        for (i in 1..50) {
            val varEnvironment = mapOf("a" to listOf(2), "b" to listOf(0), "c" to listOf(5))
            val result = testSynthesizingExpressionHelper(varEnvironment,true)
            assertEquals(true, result, "lists of single value, true expression")
        }
    }

    @Test
    fun testSynthesizingTrueExpressionSeveralValues() {
        for (i in 1..50) {
            val varEnvironment = mapOf("one" to listOf(-100, 5, 90, 1000000, -1000034),
                    "b" to listOf(0, Int.MAX_VALUE - 1, Int.MIN_VALUE + 1, 42), "c" to listOf(1111111, 5, 96, 123),
                    "fifth" to listOf(-8, 8, 8436, -354612423))

            val result = testSynthesizingExpressionHelper(varEnvironment,true)
            assertEquals(true, result, "lists of several values, true expression")
        }
    }

    @Test
    fun testSynthesizingFalseExpressionSingleValue() {
        for (i in 1..50) {
            val varEnvironment = mapOf("a" to listOf(2), "b" to listOf(0), "c" to listOf(5))
            val result = testSynthesizingExpressionHelper(varEnvironment,false)

            assertEquals(false, result, "lists of single values, false expression")
        }
    }

    @Test
    fun testSynthesizingFalseExpressionSeveralValues() {
        for (i in 1..50) {
            val varEnvironment = mapOf("one" to listOf(-100, 5, 90, 1000000, -1000034),
                    "b" to listOf(0, Int.MAX_VALUE - 1, Int.MIN_VALUE + 1, 42), "c" to listOf(1111111, 5, 96, 123),
                    "fifth" to listOf(-8, 8, 8436, -354612423))

            val result = testSynthesizingExpressionHelper(varEnvironment,false)
            assertEquals(false, result, "lists of several values, true expression")
        }
    }

    fun testSynthesizingExpressionHelper(varEnvironment : Map<String, List<Int>>, expected : Boolean) : Boolean {
        var synthesizedPredicate = SynthesizePredicate().synPredicate(varEnvironment, expected, 2)
        for (variable in varEnvironment.keys) {
            synthesizedPredicate = synthesizedPredicate.replace(variable, varEnvironment[variable]?.last().toString())
        }

        val sem = ScriptEngineManager()
        val se = sem.getEngineByName("JavaScript")
        val result = se.eval(synthesizedPredicate)

        require(result is Boolean)
        return result
    }
}