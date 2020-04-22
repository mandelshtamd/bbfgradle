package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtIfExpression
import java.util.*

class AddAlwaysFalseConditionalBlock : EquivalentMutation() {
    override fun transform() { //сначала генерируем условие, затем блок в условии
        val text = file.text.lines().toMutableList()
        val maxNum = file.text.lines().size / 2

        repeat(Random().nextInt(maxNum)) {
            val changeLine = Random().nextInt(file.text.lines().size)

            var falseConditionalBlock = falseConditionalBlock(text[changeLine], changeLine)
            var code_backup = text[changeLine]
            text.addAll(changeLine, falseConditionalBlock)
            text.removeAt(changeLine + falseConditionalBlock.size)
            println(getText(text))

            if (!checker.checkTextCompiling(getText(text))) {
                for (i in 1..falseConditionalBlock.size)
                    text.removeAt(changeLine)
                text.add(changeLine, code_backup)
            }
        }

        file = psiFactory.createFile(getText(text))



//        """var backup_v : Int = ${SynthesizeValidExpression().SynExpr(env)}
//        if (${SynthesizePredicate().SynPred(env, true, 2)}) {
//            backup_v = v
//            v = ${SynthesizeValidExpression().SynExpr(env)}
//            if/while (${SynthesizePredicate().SynPred(env, false, 2)}) {
//                println(v)
//            }
//        }
//        v = backup_v"""
//
//        val environment = VarEnvironment().getMapOfVarsAndValues()
//        val boolexpr = SynthesizePredicate().SynPred(environment, true, 2)
//
//        val res = psiFactory.createExpression("if ($boolexpr) {${'$'}}} else {")
//                as KtIfExpression
    }

    fun falseConditionalBlock(code : String, line : Int) : List<String> {
        val creator = PSICreator("")
        val compiler = JVMCompiler()
        val psi = creator.getPSIForText(Transformation.file.text)
        val checker = MutationChecker(compiler)
        VariableValuesTracer(psi, creator.ctx!!, checker).trace(line)
        val res = RuntimeVariableValuesCollector(psi, compiler).collect()

        return """    if (${SynthesizePredicate().SynPred(res, false, 2)}) {
        $code 
    }""".split('\n')
    }

    private fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")
}