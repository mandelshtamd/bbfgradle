package com.stepanov.bbf.bugfinder.metamorphicMutator
import com.intellij.psi.PsiFile
import com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations.*

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
        executeMutation(MutateArithmeticExpression(),   100)
        println("arithmetic expr ended")
        println(file.text)
//        executeMutation(AddAlwaysTrueGuard(), 100)
//        println("atg ended")
//        println(file.text)
        executeMutation(AddAlwaysTrueConditionalBlock(), 100)
        println("atc ended")
        println(file.text)
        executeMutation(AddAlwaysFalseConditionalBlock(), 100)
        println("fcb ended")
        println(file.text)
    }
}