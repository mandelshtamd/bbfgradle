package com.stepanov.bbf.bugfinder.metamorphicMutator

import com.stepanov.bbf.bugfinder.executor.compilers.JSCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.executor.debugger.RuntimeVariableValuesCollector
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.SynthesizePredicate
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.SynthesizeValidExpression
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation.Companion.checker
import com.stepanov.bbf.bugfinder.tracer.VariableValuesTracer
import com.stepanov.bbf.bugfinder.util.BBFProperties
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllChildren
import com.stepanov.bbf.reduktor.util.getAllDFSChildren
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFunction
import java.io.File
import java.lang.StringBuilder
import java.util.*


fun main() {
    val pathToTestProgram = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/seedPrograms/seed1.kt"
    Factory.file = PSICreator("").getPSIForFile(pathToTestProgram)
    val psiCreator = PSICreator("")
    val psiFile = psiCreator.getPSIForFile(pathToTestProgram)

    val backends = BBFProperties.getStringGroupWithoutQuotes("BACKEND_FOR_REDUCE").entries
    val compilers = backends.map { back ->
            when {
                back.key.startsWith("JVM") -> JVMCompiler(back.value)
                back.key.startsWith("JS") -> JSCompiler(back.value)
                else -> throw IllegalArgumentException("Illegal backend")
            }
        }

    checker = MutationChecker(compilers)
    MetamorphicMutator(psiFile, psiCreator.ctx!!).startMutate()

    saveResult()
}


fun saveResult() {
    val resultingMutant = PSICreator("").getPSIForText(Transformation.file.text)
    val pathToMutatedProgram = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/mutatedPrograms"
    val pathToSave = "$pathToMutatedProgram/${Random().getRandomVariableName(5)}.kt"

    File(pathToSave).writeText(resultingMutant.text)
}