package com.stepanov.bbf.bugfinder.manager

import com.stepanov.bbf.bugfinder.Reducer
import com.stepanov.bbf.bugfinder.executor.CommonCompiler
import com.stepanov.bbf.bugfinder.executor.CompilationChecker
import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.util.FilterDuplcatesCompilerErrors
import com.stepanov.bbf.bugfinder.util.moveAllCodeInOneFile
import com.stepanov.bbf.bugfinder.util.saveOrRemoveToTmp
import org.apache.log4j.Logger

enum class BugType {
    BACKEND,
    FRONTEND,
    DIFFBEHAVIOR,
    UNKNOWN,
    DIFFCOMPILE,
    METAMORPHIC
}

data class Bug(val compilers: List<CommonCompiler>, val msg: String, val crashedProject: Project, val type: BugType) {

    constructor(compiler: CommonCompiler, msg: String, crashedProject: Project, type: BugType) : this(
        listOf(compiler),
        msg,
        crashedProject,
        type
    )

    val compilerVersion = compilers.joinToString(", ")

    fun compareTo(other: Bug): Int =
        if (compilerVersion == other.compilerVersion)
            type.compareTo(other.type)
        else compilerVersion.compareTo(other.compilerVersion)

    fun getDirWithSameTypeBugs(): String =
        CompilerArgs.resultsDir +
                when (type) {
                    BugType.DIFFBEHAVIOR -> "diffBehavior"
                    BugType.DIFFCOMPILE -> "diffCompile"
                    BugType.FRONTEND, BugType.BACKEND, BugType.METAMORPHIC -> compilers.first().compilerInfo.filter { it != ' ' }
                    else -> ""
                }

    override fun toString(): String {
        return "${type.name}\n${compilers.map { it.compilerInfo }}\nText:\n${crashedProject.getCommonTextWithDefaultPath()}"
    }

}


object BugManager {
    private val bugs = mutableListOf<Bug>()

    fun saveBug(
        compilers: List<CommonCompiler>,
        msg: String,
        crashedProject: Project,
        type: BugType = BugType.UNKNOWN
    ) {
        val bug =
            if (type == BugType.UNKNOWN)
                Bug(
                    compilers,
                    msg,
                    crashedProject,
                    parseTypeOfBugByMsg(msg)
                )
            else
                Bug(compilers, msg, crashedProject, type)
        saveBug(bug)
    }

    fun checkIfBugIsProject(bug: Bug): Bug =
        if (bug.type != BugType.BACKEND && bug.type != BugType.FRONTEND) bug
        else if (bug.crashedProject.texts.size > 1) {
            val checker = CompilationChecker(bug.compilers)
            val oneFileBugs = checker.isCompilerBug(bug.crashedProject.moveAllCodeInOneFile())
            if (oneFileBugs.isNotEmpty()) Bug(
                bug.compilers,
                bug.msg,
                bug.crashedProject.moveAllCodeInOneFile(),
                bug.type
            )
            else bug
        } else bug


    fun saveBug(bug: Bug) {
        //Check if bug is real project bug
        val newBug = checkIfBugIsProject(bug)
        val reduced = Reducer.reduce(newBug, false)
        val reducedBug = Bug(newBug.compilers, newBug.msg, reduced, newBug.type)
        //Try to find duplicates
        //TODO Make for projects!!
        if (newBug.crashedProject.texts.size == 1 &&
            CompilerArgs.shouldFilterDuplicateCompilerBugs &&
            haveDuplicates(reducedBug)
        ) return
        bugs.add(reducedBug)
        //Report bugs
        if (ReportProperties.getPropAsBoolean("TEXT_REPORTER") == true) {
            TextReporter.dump(bugs)
        }
        if (ReportProperties.getPropAsBoolean("FILE_REPORTER") == true) {
            FileReporter.dump(listOf(reducedBug))
        }
    }

    fun haveDuplicates(bug: Bug): Boolean {
        val dirWithSameBugs = bug.getDirWithSameTypeBugs()
        val path = bug.crashedProject.saveOrRemoveToTmp(true)
        when (bug.type) {
            BugType.DIFFCOMPILE -> return FilterDuplcatesCompilerErrors.haveSameDiffCompileErrors(
                path,
                dirWithSameBugs,
                bug.compilers,
                true
            )
            BugType.FRONTEND, BugType.BACKEND -> return FilterDuplcatesCompilerErrors.simpleHaveDuplicatesErrors(
                path,
                dirWithSameBugs,
                bug.compilers.first()
            )
        }
        bug.crashedProject.saveOrRemoveToTmp(false)
        return false
    }

    private fun parseTypeOfBugByMsg(msg: String): BugType =
        if (msg.contains("Exception while analyzing expression"))
            BugType.FRONTEND
        else
            BugType.BACKEND

    private val log = Logger.getLogger("bugFinderLogger")
}

