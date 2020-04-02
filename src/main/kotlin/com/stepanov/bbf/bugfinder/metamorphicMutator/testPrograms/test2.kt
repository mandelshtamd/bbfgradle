package com.stepanov.bbf.bugfinder.metamorphicMutator.testPrograms

import java.lang.StringBuilder

class Main {
}

fun main() {
    println(checkEnvelopes(1, 3, 7, 2))
    println(sumOfEvenNumbers())
    println(getBoolean(7))
}

fun checkEnvelopes(a: Int, b: Int, c: Int, d: Int) : Boolean {
    return when{
        kotlin.math.max(a,b) > kotlin.math.max(c, d) -> kotlin.math.min(a, b) > kotlin.math.min(c, d)
        kotlin.math.max(a,b) < kotlin.math.max(c, d) -> kotlin.math.min(c, d) > kotlin.math.min(a, b)
        else -> false
    }
}

fun sumOfEvenNumbers() : Int {
    var sum = 0
    for(n in 2..99 step 2) {
        sum += n
    }
    return sum
}

fun getBoolean(n: Int) : String {
    val answer = StringBuilder()
    var divN = n
    while(divN > 0) with(answer) {
        val reminder = divN%2
        divN /= 2
        insert(0,reminder)
    }
    return answer.toString()
}

//Random test program from github