package com.stepanov.bbf.bugfinder.metamorphicMutator.tests

import com.stepanov.bbf.bugfinder.SingleFileBugFinder
import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.compilers.JSCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.metamorphicMutator.MetamorphicMutator
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddArithmeticExpression
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.BBFProperties
import com.stepanov.bbf.bugfinder.util.checkCompilingForAllBackends
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.bugfinder.util.noBoxFunModifying
import com.stepanov.bbf.reduktor.parser.PSICreator
import java.io.File
import java.util.*
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    Factory.file = PSICreator("").getPSIForFile("src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/tests/test1.kt")
    val psiCreator = PSICreator("")
    val PsiFile = psiCreator.getPSIForFile("src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/tests/test1.kt")

    val backends = BBFProperties.getStringGroupWithoutQuotes("BACKEND_FOR_REDUCE").entries
    val compilers = backends.map { back ->
            when {
                back.key.startsWith("JVM") -> JVMCompiler(back.value)
                back.key.startsWith("JS") -> JSCompiler(back.value)
                else -> throw IllegalArgumentException("Illegal backend")
            }
        }

    Transformation.checker = MutationChecker(compilers)
    MetamorphicMutator(PsiFile, psiCreator.ctx!!).startMutate()

    val resultingMutant = PSICreator("").getPSIForText(Transformation.file.text)

    val pathToNewTests = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/tests/progs"
    val pathToSave = "$pathToNewTests/${Random().getRandomVariableName(10)}.kt"
    File(pathToSave).writeText(resultingMutant.text)
}