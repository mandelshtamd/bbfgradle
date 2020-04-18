package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFunction
import java.io.File

abstract class EquivalentMutation : Transformation() {
    open class VarEnvironment {
        fun VarEnvironment() {
        }

        fun getListOfGlobalVars() : List<String>  {
            VariableValuesTracer(psi, creator.ctx!!, checker).trace(psi.lastChild)
            val res = RuntimeVariableValuesCollector(psi, compiler).collect()
            val global_vars = res.filter { (key) -> key.startsWith("globalVar") }
            return res.keys.toList()
        }

        fun getMapOfVarsAndValues() : Map<String, List<Int>> {
            VariableValuesTracer(psi, creator.ctx!!, checker).trace(psi.lastChild)
            val res = RuntimeVariableValuesCollector(psi, compiler).collect()
            return res
        }


        val creator = PSICreator("")
        val compiler = JVMCompiler()
        val psi = creator.getPSIForText(file.text)
        val checker = MutationChecker(compiler)
    }

}