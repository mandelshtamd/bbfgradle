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

            var trueConditionalBlock = trueConditionalBlock(changeLine)
            if (trueConditionalBlock.isEmpty())
                return

            text.addAll(changeLine, trueConditionalBlock)
            text.removeAt(changeLine + trueConditionalBlock.size)
            println(getText(text))

            if (!checker.checkTextCompiling(getText(text))) {
                for (i in 1..3)
                    text.removeAt(changeLine)
            }
        }

        file = psiFactory.createFile(getText(text))
    }

    fun trueConditionalBlock(line : Int) : List<String> {
        val env = getVarEnv(1, line)

        if (env.size < 1)
            return emptyList()

        val changedVar = SynthesizeValidExpression().Sample(env.keys, 1).first()

        val exprKeyWord = when(Random().nextInt(2)) {
            0 -> "if"
            else -> "while"
        }

        val result = """    var backup_$changedVar : Int = ${SynthesizeValidExpression().SynExpr(env)}
        if (${SynthesizePredicate().SynPred(env, true, 2)}) {
            backup_$changedVar = $changedVar
            $changedVar = ${SynthesizeValidExpression().SynExpr(env)}
            $exprKeyWord (${SynthesizePredicate().SynPred(env, false, 2)}) {
                println($changedVar)
            }
        }
        $changedVar = backup_$changedVar""".split('\n')

        return result
    }

    fun getVarEnv(beginLine : Int = 1, endLine : Int) : MutableMap<String, List<Int>> {
        val creator = PSICreator("")
        val compiler = JVMCompiler()
        val psi = creator.getPSIForText(Transformation.file.text)
        val checker = MutationChecker(compiler)
        VariableValuesTracer(psi, creator.ctx!!, checker).trace(beginLine)

        val res = RuntimeVariableValuesCollector(psi, compiler).collect().toMutableMap()

        for (line in beginLine+1 .. endLine) {
            VariableValuesTracer(psi, creator.ctx!!, checker).trace(line)
            val lineResult = RuntimeVariableValuesCollector(psi, compiler).collect()
            res.putAll(lineResult)
        }
        return res
    }

    private fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")
}