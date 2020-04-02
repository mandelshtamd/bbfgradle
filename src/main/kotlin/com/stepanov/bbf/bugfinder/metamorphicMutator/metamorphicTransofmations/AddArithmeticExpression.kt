package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import com.stepanov.bbf.bugfinder.util.getRandomBoolean
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.fir.lightTree.converter.generateDestructuringBlock
import org.jetbrains.kotlin.psi.psiUtil.parents
import kotlin.random.Random

class AddArithmeticExpression : Transformation() {
    override fun transform() {
        val operators = file.node.getAllChildrenNodes()
            .filter { it.elementType == KtNodeTypes.INTEGER_CONSTANT }
        operators.forEach {
            /* генерирую два операнда, заменяю */
            val newArithmExpr = transformArithmeticExpr(it.text.toInt())
            replaceArithmExpr(it, newArithmExpr, false)
        }
    }

    private fun transformArithmeticExpr(source : Int): String {
        var newArithmExpr = "$source"
        when (Random.nextInt() % 6) {
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
        val rightOp = source - leftOp
        return "($leftOp + $rightOp)"
    }

    private fun replaceArithmExpr(replace: ASTNode, replacement: String, isRandom: Boolean = true) {
        if (isRandom && getRandomBoolean() || !isRandom) {
            val replacementNode =
                psiFactory.createArgument(replacement)

            checker.replacePSINodeIfPossible(file, replace.psi, replacementNode)
        }
    }
}