package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import org.jetbrains.kotlin.KtNodeTypes
import kotlin.random.Random

class MutateArithmeticExpression : Transformation() {
    val mutationsCount = 6

    override fun transform() {
        val operators = file.node.getAllChildrenNodes()
            .filter { it.elementType == KtNodeTypes.INTEGER_CONSTANT }
        operators.forEach {
            val mutatedArithmExpr = doMutation(it.text.toInt())
            replaceArithmExpr(it, mutatedArithmExpr)
        }
    }

    private fun doMutation(source : Int): String {
        var newArithmExpr = "$source"
        when (Random.nextInt() % mutationsCount) {
            0 -> newArithmExpr = breakdownIntoTerms(source)
            1 -> newArithmExpr = "($source or ($source % 2))"
            2 -> newArithmExpr = "($source and 0.inv())"
            3 -> newArithmExpr = "(($source shl 1) shr 1)"
            4 -> newArithmExpr = "($source shl 0)"
            5 -> newArithmExpr = "($source + ($source.inv() and $source))"
        }
        return newArithmExpr
    }

    private fun breakdownIntoTerms(source: Int) : String {
        val leftOp = Random.nextInt() % 100000
        var rightOp = 0
        if (source - leftOp < Int.MIN_VALUE) { 
            rightOp = source + leftOp 
            return "(-$leftOp + $rightOp)"
        } else { 
            rightOp = source - leftOp 
            return "($leftOp + $rightOp)"
        }
    }

    private fun replaceArithmExpr(replace: ASTNode, replacement: String) {
        val replacementNode = psiFactory.createArgument(replacement)
        checker.replacePSINodeIfPossible(file, replace.psi, replacementNode)
    }
}
