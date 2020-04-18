package com.stepanov.bbf.bugfinder.metamorphicMutator.metamorphicTransofmations

import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import com.stepanov.bbf.bugfinder.util.getAllChildrenNodes
import org.jetbrains.kotlin.KtNodeTypes

class AddAlwaysFalseConditionalBlock : EquivalentMutation() {
    override fun transform() { //сначала генерируем условие, затем блок в условии
        val operators = Transformation.file.node.getAllChildrenNodes()
                .filter { it.elementType == KtNodeTypes.PROPERTY }
        //TODO()
        val extraNode = psiFactory.createProperty("a")

    }
}