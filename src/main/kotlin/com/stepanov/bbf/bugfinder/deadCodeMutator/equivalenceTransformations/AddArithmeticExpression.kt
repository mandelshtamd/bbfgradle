package com.stepanov.bbf.bugfinder.deadCodeMutator.equivalenceTransformations

import com.intellij.lang.ASTNode
import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.*
import org.jetbrains.kotlin.psi.*

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.resolve.calls.smartcasts.IdentifierInfo
import java.util.*

class AddArithmeticExpression : Transformation() {
    override fun transform() {
        val operators = file.node.getAllChildrenNodes()
            .filter { it.treeParent.elementType == KtNodeTypes.INTEGER_CONSTANT }
        operators.forEach {
            replaceOperator(it, "($it - 5) + 5")
        }
    }

    private fun transformArithmeticExpr() : Int = TODO()

    private fun replaceOperator(replace: ASTNode, replacement: String, isRandom: Boolean = true) {
        if (isRandom && getRandomBoolean() || !isRandom) {
            val replacementNode =
                    psiFactory.createOperationName(replacement)

            checker.replacePSINodeIfPossible(file, replace.psi, replacementNode)
        }
    }
}

//ПОСМОТРЕТЬ
//class ChangeArgToAnotherValue : Transformation() {
//
//    //TODO For user classes
//    override fun transform() {
//        file.getAllPSIChildrenOfType<KtNamedFunction>().forEach { f ->
//            getAllInvocations(f).forEach { inv ->
//                inv.valueArguments.forEachIndexed { argInd, arg ->
//                    if (argInd < f.valueParameters.size) {
//                        val type = f.valueParameters[argInd].typeReference?.text ?: return@forEachIndexed
//                        val newRandomValue = generateDefValuesAsString(type)
//                        if (newRandomValue.isEmpty()) return@forEachIndexed
//                        val newArg = psiFactory.createArgument(newRandomValue)
//                        checker.replacePSINodeIfPossible(file, arg, newArg)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getAllInvocations(func: KtNamedFunction): List<KtCallExpression> {
//        val res = mutableListOf<KtCallExpression>()
//        file.getAllPSIChildrenOfType<KtCallExpression>()
//            .filter {
//                it.getCallNameExpression()?.getReferencedName() == func.name &&
//                        it.valueArguments.size == func.valueParameters.size
//            }
//            .forEach { res.add(it) }
//        return res
//    }
//}