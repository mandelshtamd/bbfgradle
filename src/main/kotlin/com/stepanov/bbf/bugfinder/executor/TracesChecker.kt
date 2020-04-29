package com.stepanov.bbf.bugfinder.executor

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.stepanov.bbf.bugfinder.executor.compilers.JCompiler
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.util.getAllChildrenNodes
import org.apache.log4j.Logger
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.ImportPath
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

// Transformation is here only for PSIFactory
class TracesChecker(private val compilers: List<CommonCompiler>) : CompilationChecker(compilers) {

    private companion object FalsePositivesTemplates {
        //Regex and replacing
        val exclErrorMessages = listOf(
            "IndexOutOfBoundsException"
        )
    }

    fun checkTest(text: String): List<CommonCompiler>? {
        var resText = text
        if (!resText.contains("fun main(")) {
            resText += "fun main(args: Array<String>) {\n" +
                    "    println(box())\n" +
                    "}"
        }
        val writer = BufferedWriter(FileWriter(CompilerArgs.pathToTmpFile))
        writer.write(resText)
        writer.close()
        val res = checkTest(resText, CompilerArgs.pathToTmpFile)
        File(CompilerArgs.pathToTmpFile).delete()
        return res
    }

    fun addMainForKJavaProject(project: Project) =
        Project(project.texts
            .map { it to it.getFileLanguageIfExist() }
            .map { if (it.second == LANGUAGE.KOTLIN) it.first to psiFactory.createFile(it.first) else it.first to null }
            .map {
                if (it.second?.getAllPSIChildrenOfType<KtNamedFunction>()
                        ?.any { it.name?.contains("box") == true } == true
                ) addMain(it.first) else
                    it.first
            }, null, LANGUAGE.KJAVA
        )


    private fun addMain(text: String): String =
        text + "\nfun main(args: Array<String>) {\n" +
                "    println(box())\n" +
                "}"

    fun addMainForProject(project: Project): Project {
        if (project.language == LANGUAGE.KJAVA) return addMainForKJavaProject(project)
        if (project.texts.size == 1) {
            val newText = addMain(project.texts.first())
            return Project(listOf(newText))
        } else {
            val files = project.texts.map { psiFactory.createFile(it) }
            val boxFuncs = files.map { file ->
                file.getAllPSIChildrenOfType<KtNamedFunction>().find { it.name?.contains("box") ?: false }!!
            }
            //Add import of box_I functions
            val firstFile = files.first()
            boxFuncs.forEachIndexed { i, func ->
                val `package` = (func.parents.find { it is KtFile } as KtFile).packageDirective?.fqName
                    ?: throw IllegalArgumentException("No package")
                val newImport =
                    psiFactory.createImportDirective(ImportPath(FqName("${`package`}.${func.name}"), false))
                firstFile.addImport(newImport)
            }
            firstFile.addMain(boxFuncs)
            return Project(null, files)
        }
    }

    fun compareTraces(project: Project): List<CommonCompiler>? {
        val path = project.generateCommonName()
        //Check if already checked
        val text = project.getCommonText(path)
        val hash = text.hashCode()
        if (alreadyChecked.containsKey(hash)) {
            log.debug("ALREADY CHECKED!!!")
            return alreadyChecked[hash]!!
        }

        //Add main
        val projectWithMain = addMainForProject(project)
        if (!isCompilationSuccessful(projectWithMain)) {
            log.debug("Cant compile with main")
            log.debug("Proj = ${projectWithMain.getCommonTextWithDefaultPath()}")
            return null
        }
        projectWithMain.saveOrRemoveToTmp(true)
        val results = mutableListOf<Pair<CommonCompiler, String>>()
        for (comp in compilers) {
            val status = comp.compile(path)
            if (status.status == -1)
                return null
            val res = comp.exec(status.pathToCompiled)
            val errors = comp.exec(status.pathToCompiled, Stream.ERROR)
            log.debug("Result of ${comp.compilerInfo}: $res\n")
            log.debug("Errors: $errors")
            if (exclErrorMessages.any { errors.contains(it) })
                return null
            results.add(comp to res.trim())
        }
        val groupedRes = results.groupBy({ it.second }, valueTransform = { it.first })
        return if (groupedRes.size == 1) {
            null
        } else {
            val res = groupedRes.map { it.value.first() }
            alreadyChecked[hash] = res
            res
        }
    }

    fun compareTracesOfFiles(files: List<PsiFile>): Map<CommonCompiler, List<PsiFile>> {
        val res = mutableMapOf<CommonCompiler, List<PsiFile>>()
        for (compiler in compilers) {
            val r = files
                .map { addMainWithBoxInvoke(it) }
                .map { it to compileAndExec(compiler, it) }
                .groupBy({ it.second }, { it.first })
            if (r.size != 1) res[compiler] = r.values.map { it.first() }
        }
        return res
    }

    fun checkTestForProject(commonPath: String): List<CommonCompiler>? {
        val results = mutableListOf<Pair<CommonCompiler, String>>()
        for (comp in compilers) {
            val status = comp.compile(commonPath)
            if (status.status == -1)
                return null
            val res = comp.exec(status.pathToCompiled)
            val errors = comp.exec(status.pathToCompiled, Stream.ERROR)
            log.debug("Result of ${comp.compilerInfo}: $res\n")
            log.debug("Errors: $errors")
            results.add(comp to res.trim())
        }
        val groupedRes = results.groupBy({ it.second }, valueTransform = { it.first })
        return if (groupedRes.size == 1) {
            null
        } else {
            groupedRes.map { it.value.first() }
        }
    }

    private fun compileAndExec(compiler: CommonCompiler, file: PsiFile): String {
        val tmpPath = Project(file.text).saveOrRemoveToTmp(true)
        val compileStatus = compiler.compile(tmpPath)
        if (compileStatus.status == -1) return ""
        Project(file.text).saveOrRemoveToTmp(false)
        return compiler.exec(compileStatus.pathToCompiled)
    }

    fun checkTest(text: String, pathToFile: String): List<CommonCompiler>? {
        val hash = text.hashCode()
        if (alreadyChecked.containsKey(hash)) {
            log.debug("ALREADY CHECKED!!!")
            return alreadyChecked[hash]!!
        }

        val psiFile = psiFactory.createFile(text)
        //Check for syntax correctness
        if (psiFile.node.getAllChildrenNodes().any { it.psi is PsiErrorElement }) {
            log.debug("Not correct syntax")
            alreadyChecked[hash] = null
            return null
        }

        log.debug("Trying to compile with main function:")
        if (!compilers.checkCompilingForAllBackends(psiFile)) {
            log.debug("Cannot compile with main")
            return null
        }

        log.debug("Executing traced code:\n$text")
        val results = mutableListOf<Pair<CommonCompiler, String>>()
        for (comp in compilers) {
            val status = comp.compile(pathToFile)
            if (status.status == -1)
                return null
            val res = comp.exec(status.pathToCompiled)
            val errors = comp.exec(status.pathToCompiled, Stream.ERROR)
            log.debug("Result of ${comp.compilerInfo}: $res\n")
            log.debug("Errors: $errors")
            if (FalsePositivesTemplates.exclErrorMessages.any { errors.contains(it) })
                return null
            results.add(comp to res.trim())
        }
        //Compare with java
        if (CompilerArgs.useJavaAsOracle) {
            val res = JCompiler().compile(pathToFile)
            if (res.status == 0) {
                val execRes = JCompiler().exec(res.pathToCompiled, Stream.BOTH)
                log.debug("Result of JAVA: $execRes")
                results.add(JCompiler() to execRes.trim())
            } else log.debug("Cant compile with Java")
        }
        val groupedRes = results.groupBy({ it.second }, valueTransform = { it.first }).toMutableMap()
        return if (groupedRes.size == 1) {
            null
        } else {
            val res = groupedRes.map { it.value.first() }
            alreadyChecked[hash] = res
            res
        }
    }

    private fun addMainWithBoxInvoke(psiFile: PsiFile): PsiFile {
        if (psiFile.text.contains("fun main(")) return psiFile.copy() as PsiFile
        val factory = KtPsiFactory(psiFile.project)
        val mainFun = """
        fun main(args: Array<String>) {
            println(box())
        }
    """.trimIndent()
        return factory.createFile(psiFile.text + "\n" + mainFun)
    }

    var alreadyChecked: HashMap<Int, List<CommonCompiler>?> = HashMap()
    private val log = Logger.getLogger("bugFinderLogger")
}
