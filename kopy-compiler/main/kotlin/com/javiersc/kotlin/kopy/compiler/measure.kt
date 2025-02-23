@file:OptIn(ExperimentalUuidApi::class)

package com.javiersc.kotlin.kopy.compiler

import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.util.render

internal inline fun <T> measureExecution(
    isEnabled: Boolean = true,
    key: String,
    block: () -> T,
): T = if (isEnabled) measureTimedValue(block).also { it.log(key) }.value else block()

internal inline fun <T> TimedValue<T>.log(key: String): TimedValue<T> {
    val previous: List<TimedValue<Any>> = measurements[key].orEmpty()
    measurements.put(key, previous + this@log as TimedValue<Any>)
    return this
}

internal val measurements: MutableMap<String, List<TimedValue<Any>>> = mutableMapOf()

internal fun MutableMap<String, List<TimedValue<Any>>>.createReport(isEnabled: Boolean = true) {
    if (!isEnabled) return
    val reportContent = buildString {
        var total: Duration = 0.milliseconds
        for ((key: String, values: List<TimedValue<Any>>) in this@createReport) {
            val values: Sequence<Pair<String, Duration>> =
                values.asSequence().map {
                    val value = it.value
                    total = total + it.duration
                    when (value) {
                        is FirElement -> value.render() to it.duration
                        is IrElement -> value.render() to it.duration
                        else -> value.toString() to it.duration
                    }
                }
            val valuesToRender: String =
                values.map { "${it.first}, ${it.second}" }.toList().joinToString("\n") { "  - $it" }
            appendLine("$key:\n$valuesToRender")
        }
        appendLine()
        appendLine("TOTAL: $total")
    }
    val file: File =
        File("D:\\repo\\kotlin\\kopy\\build\\reports\\kopy\\metrics_${Uuid.random()}").apply {
            parentFile.mkdirs()
            createNewFile()
        }
    file.writeText(reportContent)
}

internal fun main() {
    File("D:\\repo\\kotlin\\kopy\\build\\reports\\kopy")
        .walkTopDown()
        .filter { it.isFile }
        .mapNotNull { it.readLines().lastOrNull { it.startsWith("TOTAL:") } }
        .sumOf { Duration.parse(it.substringAfter("TOTAL: ")).inWholeMilliseconds }
        .also(::println)
}
