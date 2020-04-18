package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import java.util.*

class AddAlwaysTrueGuard : EquivalentMutation() {
    override fun transform() {
        val environment = VarEnvironment().getMapOfVarsAndValues()
        val names = VarEnvironment().getListOfGlobalVars()

        val boolexpr = SynthesizePredicate().SynPred(environment, true, 2)

        val res = psiFactory.createExpression("if ($boolexpr) {$}} else {")
                as KtIfExpression
        val block = psiFactory.createBlock(res.text)
        //Remove braces
        block.deleteChildInternal(block.lBrace!!.node)
        block.deleteChildInternal(block.rBrace!!.node)
    }
}