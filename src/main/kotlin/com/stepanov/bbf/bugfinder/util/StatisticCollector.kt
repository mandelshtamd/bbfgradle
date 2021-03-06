package com.stepanov.bbf.bugfinder.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object StatisticCollector {

    private val path = "logs/stats.log"
    val content: StatisticContent
    var curMutation: MutationStat? = null

    init {
        content = if (File(path).exists()) deserialize() else StatisticContent()
    }

    fun setCurMutation(name: String) {
        content.mutations.find { it.name == name } ?.let {
            curMutation = it
            return
        }
        content.mutations.add(MutationStat(name, 0, 0, 0))
        curMutation = content.mutations.find { it.name == name }!!
        return
    }


    fun serializeAndSave() {
        val content = Json.stringify(StatisticContent.serializer(), content)
        File(path).writeText(content)
    }

    fun clear() {
        content.mutations.clear()
        content.overall.foundBugs = 0
        content.overall.foundDuplicates = 0
        content.overall.numOfFiles = 0
        serializeAndSave()
    }

    private fun deserialize(): StatisticContent {
        return try {
            val content = File(path).readText()
            Json.parse(StatisticContent.serializer(), content)
        } catch (e: Exception) {
            StatisticContent()
        }
    }

    fun printStats() {
        content.mutations.forEach { mut ->
            println("Mutation ${mut.name}\n" +
                    "OK: ${mut.okMut}\n" +
                    "Not OK: ${mut.notOkMut}\n" +
                    "Bugs: ${mut.foundBugsByTransformation}\n\n")
        }
        val overall = content.overall
        println("OVERALL:" +
                "Files: ${overall.numOfFiles}\n" +
                "Bugs: ${overall.foundBugs}\n" +
                "Duplicates: ${overall.foundDuplicates}")
    }

}


@Serializable
class StatisticContent {
    val mutations = mutableListOf<MutationStat>()
    val overall = OverallStatistics(0, 0, 0)
}

@Serializable
data class MutationStat(
    val name: String, var okMut: Int,
    var notOkMut: Int, var foundBugsByTransformation: Int
)

@Serializable
data class OverallStatistics(
    var numOfFiles: Int, var foundBugs: Int,
    var foundDuplicates: Int
)