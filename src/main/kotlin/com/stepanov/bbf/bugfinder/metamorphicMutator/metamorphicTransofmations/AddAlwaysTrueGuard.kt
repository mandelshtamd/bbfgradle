package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import java.util.*
import kotlin.random.Random.Default.nextInt

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        val maxNum = file.text.lines().size / 2

        repeat(Random().nextInt(maxNum)) {
            val changeLine = Random().nextInt(file.text.lines().size)

            var trueGuardBlock = trueGuardBlock(text[changeLine], changeLine)
            var code_backup = text[changeLine]
            text.addAll(changeLine, trueGuardBlock)
            text.removeAt(changeLine + trueGuardBlock.size)
            println(getText(text))

            if (!checker.checkTextCompiling(getText(text))) {
                for (i in 1..3)
                    text.removeAt(changeLine)
                text.add(changeLine, code_backup)
            }
        }

        file = psiFactory.createFile(getText(text))
    }

    fun trueGuardBlock(code : String, line : Int) : List<String> {
        val creator = PSICreator("")
        val compiler = JVMCompiler()
        val psi = creator.getPSIForText(Transformation.file.text)
        val checker = MutationChecker(compiler)
        VariableValuesTracer(psi, creator.ctx!!, checker).trace(line)
        val res = RuntimeVariableValuesCollector(psi, compiler).collect()

        return """    if (${SynthesizePredicate().SynPred(res, true, 2)}) {
        $code 
    }""".split('\n')

    }

    private fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")
}