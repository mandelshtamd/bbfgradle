package com.stepanov.bbf.bugfinder.metamorphicMutator

import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.executor.TracesChecker
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.manager.Bug
import com.stepanov.bbf.bugfinder.manager.BugManager
import com.stepanov.bbf.bugfinder.manager.BugType
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation.Companion.checker
import com.stepanov.bbf.bugfinder.tracer.Tracer
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.apache.log4j.PropertyConfigurator
import java.io.File
import java.util.*


fun main() {
    //Init log4j
    PropertyConfigurator.configure("src/main/resources/bbfLog4j.properties")
    //Init factory
    Factory.file = PSICreator("").getPSIForText("")
    //Read map from file
    val pathToTestProgram = File("tmp/arrays/metTesting/").listFiles().random().absolutePath
    println("f = $pathToTestProgram")
    Factory.file = PSICreator("").getPSIForFile(pathToTestProgram)
    val psiCreator = PSICreator("")
    val psiFile = psiCreator.getPSIForFile(pathToTestProgram)
    val compilers = listOf(JVMCompiler(), JVMCompiler("-Xuse-ir"))
    checker = MutationChecker(compilers)
    MetamorphicMutator(psiFile, psiCreator.ctx!!).startMutate()
    val mutated = Transformation.file
    val tracedOriginal = Tracer(checker, psiFile).trace()
    val tracedMutated = Tracer(checker, mutated).trace()
    val res = TracesChecker(compilers).compareTracesOfFiles(listOf(tracedOriginal, tracedMutated))
    res.forEach { BugManager.saveBug(Bug(it.key, "", Project(null, it.value), BugType.METAMORPHIC))}
    System.exit(0)
}


fun saveResult() {
    val resultingMutant = PSICreator("").getPSIForText(Transformation.file.text)
    val pathToMutatedProgram = "src/main/kotlin/com/stepanov/bbf/bugfinder/metamorphicMutator/mutatedPrograms"
    val pathToSave = "$pathToMutatedProgram/${Random().getRandomVariableName(5)}.kt"

    File(pathToSave).writeText(resultingMutant.text)
}