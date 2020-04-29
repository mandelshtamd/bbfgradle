package com.stepanov.bbf.bugfinder

import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.executor.TracesChecker
import com.stepanov.bbf.bugfinder.executor.compilers.JCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.manager.Bug
import com.stepanov.bbf.bugfinder.manager.BugManager
import com.stepanov.bbf.bugfinder.manager.BugType
import com.stepanov.bbf.bugfinder.mutator.Mutator
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.tracer.Tracer
import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.File
import java.util.*

class SingleFileBugFinder(dir: String) : BugFinder(dir) {

    fun findBugsInFile() {
        try {
            println("Let's go")
            ++counter
            log.debug("Name = $dir")
            val psiCreator = PSICreator("")
            val psiFile =
                try {
                    psiCreator.getPSIForFile(dir)
                } catch (e: Throwable) {
                    println("e = $e")
                    return
                }

            val compilersConf = BBFProperties.getStringGroupWithoutQuotes("BACKENDS")
            val filterBackends = compilersConf.map { it.key }
            val ignoreBackendsFromFile =
                psiFile.text.lineSequence()
                    .filter { it.startsWith("// IGNORE_BACKEND:") }
                    .map { it.substringAfter("// IGNORE_BACKEND:") }
                    .map { it.split(",") }
                    .flatten()
                    .map { it.trim() }
                    .toList()
            if (ignoreBackendsFromFile.any { filterBackends.contains(it) }) {
                log.debug("Skipped because one of the backends is ignoring")
                return
            }
            if (psiFile.getAllPSIChildrenOfType<KtNamedFunction>().all { it.name?.contains("box") == false }) return

            //Init lateinit vars
            Factory.file = psiFile
            Transformation.checker = MutationChecker(compilers)

            //Check for compiling
            if (!compilers.checkCompilingForAllBackends(psiFile)) {
                log.debug("Could not compile $dir")
                return
            }
            log.debug("Start to mutate")
            val resultingMutant = makeMutant(psiFile, psiCreator.ctx!!, null, listOf(::noBoxFunModifying))
            //Mutator(psiFile, psiCreator.ctx).startMutate()
            //val resultingMutant = PSICreator("").getPSIForText(Transformation.file.text)

            if (!compilers.checkCompilingForAllBackends(resultingMutant)) {
                log.debug("Could not compile after mutation $dir")
                log.debug(resultingMutant.text)
                System.exit(1)
            }
            log.debug("Mutated = ${resultingMutant.text}")

            //Save mutated file
            if (CompilerArgs.shouldSaveMutatedFiles) {
                val pathToNewTests = CompilerArgs.dirForNewTests
                File(pathToNewTests).mkdirs()
                val pathToSave = "$pathToNewTests/${Random().getRandomVariableName(10)}.kt"
                File(pathToSave).writeText(resultingMutant.text)
            }

            val checker = MutationChecker(compilers)
            //Now begin to trace mutated file
            val tracer = Tracer(checker, resultingMutant)
            val traced = tracer.trace()
            log.debug("Traced = ${traced.text}")
            if (!compilers.checkCompilingForAllBackends(traced)) {
                log.debug("Could not compile after tracing $dir")
                log.debug(traced.text)
            }

            val res = TracesChecker(compilers).checkTest(traced.text)
            log.debug("Result = $res")
            //Save into tmp file and reduce
            if (res != null) {
                File(CompilerArgs.pathToTmpFile).writeText(traced.text)
//                val reduced =
//                        if (CompilerArgs.shouldReduceDiffBehavior)
//                            Reducer.reduceDiffBehavior(
//                                CompilerArgs.pathToTmpFile,
//                                compilers
//                            )
//                        else
//                            traced.text
                BugManager.saveBug(res, "", Project(traced.text), BugType.DIFFBEHAVIOR)
            }
            return
        } catch (e: Error) {
            println("ERROR: $e")
            log.debug("ERROR: $e")
            return
            //System.exit(0)
        }
    }

    var counter = 0
}