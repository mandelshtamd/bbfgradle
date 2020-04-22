package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.reduktor.parser.PSICreator
import java.util.*

class AddAlwaysTrueConditionalBlock : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val maxNum = file.text.lines().size / 2

        repeat(Random().nextInt(maxNum)) {
            val changeLine = Random().nextInt(file.text.lines().size)

            val trueConditionalBlock = trueConditionalBlock(changeLine)
            if (trueConditionalBlock.isEmpty())
                return

            text.addAll(changeLine, trueConditionalBlock)

            if (!checker.checkTextCompiling(getText(text))) {
                for (i in 1..trueConditionalBlock.size)
                    text.removeAt(changeLine)
            }
        }

        file = psiFactory.createFile(getText(text))
    }

    fun trueConditionalBlock(line : Int) : List<String> {
        val env = getVarEnv(1, line)

        if (env.size < 1) {
            return emptyList()
        }

        val changedVar = SynthesizeValidExpression().toSample(env.keys, 1).first()

        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }

        val result = """    var backup_$changedVar : Int = ${SynthesizeValidExpression().synExpression(env)}
        if (${SynthesizePredicate().synPredicate(env, true, 2)}) {
            backup_$changedVar = $changedVar
            $changedVar = ${SynthesizeValidExpression().synExpression(env)}
            $exprKeyWord (${SynthesizePredicate().synPredicate(env, false, 2)}) {
                println($changedVar)
            }
        }
        $changedVar = backup_$changedVar""".split('\n')

        return result
    }
}