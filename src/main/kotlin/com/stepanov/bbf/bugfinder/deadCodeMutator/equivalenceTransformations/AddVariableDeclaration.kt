package com.stepanov.bbf.bugfinder.deadCodeMutator.equivalenceTransformations

import com.stepanov.bbf.bugfinder.mutator.transformations.Transformation
import org.jetbrains.kotlin.psi.*

import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import com.stepanov.bbf.bugfinder.util.getRandomVariableName
import java.util.*

class AddVariableDeclaration : Transformation() {
    override fun transform() {
        val text = file.text.lines().toMutableList()
        for (i in 0..Random().nextInt(shuffleConst)) {
            val insLine = Random().nextInt(text.size)
            val generateExpr = "null"
            text.add(insLine, generateExpr) //проверить
            if (!checker.checkTextCompiling(getText(text))) {
                text.removeAt(insLine) //проверить
            }
        }
        file = psiFactory.createFile(getText(text))
    }

    private fun getText(text: MutableList<String>) = text.joinToString(separator = "\n")

    private val shuffleConst = file.text.lines().size * 4
}