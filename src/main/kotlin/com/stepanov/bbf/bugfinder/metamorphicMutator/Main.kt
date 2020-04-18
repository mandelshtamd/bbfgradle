package com.stepanov.bbf.bugfinder.metamorphicMutator

import com.stepanov.bbf.bugfinder.executor.compilers.JSCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.SynthesizePredicate
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.BBFProperties
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllChildren
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFunction
import java.io.File
import java.lang.StringBuilder
import java.util.*


fun main(args: Array<String>) {
    val pathToTestProgram = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/testPrograms/test1.kt"


//    Factory.file = PSICreator("").getPSIForFile(pathToTestProgram)
//    val psiCreator = PSICreator("")
//    val psiFile = psiCreator.getPSIForFile(pathToTestProgram)
//
//    val backends = BBFProperties.getStringGroupWithoutQuotes("BACKEND_FOR_REDUCE").entries
//    val compilers = backends.map { back ->
//            when {
//                back.key.startsWith("JVM") -> JVMCompiler(back.value)
//                back.key.startsWith("JS") -> JSCompiler(back.value)
//                else -> throw IllegalArgumentException("Illegal backend")
//            }
//        }
//
//
//    val creator = PSICreator("")
//    val compiler = JVMCompiler()
//    val psi = creator.getPSIForText(Transformation.file.text)
//    val checker = MutationChecker(compiler)
//    VariableValuesTracer(psi, creator.ctx!!, checker).trace(psi.lastChild)
//    val res = RuntimeVariableValuesCollector(psi, compiler).collect()
//
//    val expr = StringBuilder()
//    expr.append(SynthesizePredicate().SynPred(res, true, 2))
//    println(expr)


//    Transformation.checker = MutationChecker(compilers)
//    MetamorphicMutator(psiFile, psiCreator.ctx!!).startMutate()
//
//    val resultingMutant = PSICreator("").getPSIForText(Transformation.file.text)
//    val pathToMutatedProgram = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/resultPrograms"
//    val pathToSave = "$pathToMutatedProgram/${Random().getRandomVariableName(7)}.kt"
//
//    File(pathToSave).writeText(resultingMutant.text)
}