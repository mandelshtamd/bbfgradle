package com.stepanov.bbf.testsMetamorphicMutator

import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.SynthesizeValidExpression
import org.junit.Test
import javax.script.ScriptEngineManager
import kotlin.test.assertEquals

class SynthesizeValidExpressionTests {
    @Test
    fun SynthesizeValidExpressionTests() {
        repeat(10) {
            val varEnvironment = mapOf("one" to listOf(-100, 5, 90, 1000000, -1000034),
                    "b" to listOf(0, Int.MAX_VALUE - 1, Int.MIN_VALUE + 1, 42), "c" to listOf(1111111, 5, 96, 123),
                    "fifth" to listOf(-8, 8, 8436, -354612423))
            var test = SynthesizeValidExpression().synExpression(varEnvironment.toMutableMap())

            for (variable in varEnvironment.keys) {
                test = test.replace(variable, varEnvironment[variable]?.last().toString())
            }

            val sem = ScriptEngineManager()
            val se = sem.getEngineByName("JavaScript")

            var isCompiled = true

            try {
                se.eval(test)
            } catch (e: Exception) {
                isCompiled = false
            }

            assertEquals(true, isCompiled)
        }
    }
}