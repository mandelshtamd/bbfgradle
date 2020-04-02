package com.stepanov.bbf.bugfinder.metamorphicMutator
import com.intellij.psi.PsiFile
import com.stepanov.bbf.bugfinder.BugFinder

import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.LANGUAGE
import com.stepanov.bbf.bugfinder.executor.Project
import com.stepanov.bbf.bugfinder.executor.TracesChecker
import com.stepanov.bbf.bugfinder.executor.compilers.MutationChecker
import com.stepanov.bbf.bugfinder.manager.BugManager
import com.stepanov.bbf.bugfinder.manager.BugType
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddArithmeticExpression
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddIfStatement
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddVariableDeclaration
import com.stepanov.bbf.bugfinder.mutator.projectTransformations.ShuffleNodes
import com.stepanov.bbf.bugfinder.mutator.transformations.*
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation.Companion.log
import com.stepanov.bbf.bugfinder.tracer.Tracer
import com.stepanov.bbf.bugfinder.util.*
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File
import java.util.*
import kotlin.random.Random

class MetamorphicMutator(val file: PsiFile, val context: BindingContext?) {
    private fun executeMutation(t: Transformation, probPercentage: Int = 50) {
        if (Random.nextInt() % 100 < probPercentage) {
            t.transform()
            Transformation.file = PSICreator("").getPSIForText(Transformation.file.text)
        }
    }

    fun startMutate() {
        Factory.file = file
        Transformation.file = file.copy() as PsiFile
        startMetamorphicMutations()
    }

    private fun startMetamorphicMutations() {
        executeMutation(AddArithmeticExpression(), 100)
        executeMutation(AddVariableDeclaration(), 33)
        executeMutation(AddIfStatement(), 33)
    }
}