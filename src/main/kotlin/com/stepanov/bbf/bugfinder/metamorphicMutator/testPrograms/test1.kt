package com.stepanov.bbf.bugfinder.metamorphicMutator.testPrograms

fun main(args: Array<String>) {
    val a = (60 and 0.inv()) + ((40 shl 1) shr 1)
    val b = (72541 + -72416)
    print(a.toString() + b.toString())
}