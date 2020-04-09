package com.stepanov.bbf.bugfinder.metamorphicMutator
import com.intellij.psi.PsiFile

import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.MutateArithmeticExpression
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddIfStatement
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.AddVariableDeclaration
import com.stepanov.bbf.bugfinder.mutator.transformations.*
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.resolve.BindingContext
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
        executeMutation(MutateArithmeticExpression(), 100)
        executeMutation(AddVariableDeclaration(), 40)
        executeMutation(AddIfStatement(), 40)
    }
}