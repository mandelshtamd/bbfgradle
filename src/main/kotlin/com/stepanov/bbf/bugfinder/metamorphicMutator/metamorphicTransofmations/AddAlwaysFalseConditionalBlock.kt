package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.reduktor.parser.PSICreator
import java.util.*

class AddAlwaysFalseConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val maxNum = file.text.lines().size / 2
        println("yes")
        repeat(Random().nextInt(maxNum)) {
            val changeLine = Random().nextInt(file.text.lines().size)

            val falseConditionalBlock = falseConditionalBlock(changeLine)
            println(falseConditionalBlock)

            if (falseConditionalBlock.isEmpty())
                return

            text.addAll(changeLine, falseConditionalBlock)

            if (!checker.checkTextCompiling(getText(text))) {
                for (i in 1..falseConditionalBlock.size)
                    text.removeAt(changeLine)
            }
        }

        file = psiFactory.createFile(getText(text))
    }

    fun falseConditionalBlock(line : Int) : List<String> {
        val env = getVarEnv(1, line)

        if (env.size < 1) {
            return emptyList()
        }

        val modifiedVar = SynthesizeValidExpression().toSample(env.keys, 1).first()

        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }

        val result =
        """$exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
            ${modifiedVar} = ${SynthesizeValidExpression().synExpression(env)}
            println($modifiedVar)
        }""".split('\n')

        return result
    }
}