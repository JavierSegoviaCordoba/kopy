@file:OptIn(ExperimentalUuidApi::class)

package com.javiersc.kotlin.kopy.compiler

import java.io.File
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.util.dumpKotlinLike

private val CompilerConfiguration.isDebugEnabled: Boolean
    get() = get(KopyKey.Debug, false)

internal class MeasureStorage : CompilerPluginRegistrar.PluginDisposable {
    private val _measures: MutableList<Measure> = mutableListOf()
    val measures: List<Measure> = _measures

    fun put(key: String, value: Any, duration: Duration, isNested: Boolean) {
        put(Measure(key = key, value = value, duration = duration, isNested = isNested))
    }

    fun put(measure: Measure) {
        _measures.add(measure)
    }

    override fun dispose() {
        _measures.clear()
    }
}

internal class Measure(
    val key: String,
    val value: Any,
    val duration: Duration,
    val isNested: Boolean = false,
)

internal val KClass<*>.measureKey: String
    get() = java.name

internal fun KClass<*>.measureKey(extra: String): String = "${java.name}$$extra"

internal inline fun <T> KopyConfig.measureExecution(
    key: String,
    isNested: Boolean = false,
    block: () -> T,
): T =
    if (configuration.isDebugEnabled) {
        val timedValue: TimedValue<T> = measureTimedValue(block)
        measureStorage.log(key = key, timedValue = timedValue, isNested = isNested)
        timedValue.value
    } else block()

internal fun <T> MeasureStorage.log(key: String, timedValue: TimedValue<T>, isNested: Boolean) {
    put(key, timedValue.value as Any, timedValue.duration, isNested = isNested)
}

internal fun KopyConfig.createReport() {
    val isEnabled: Boolean = configuration.get(KopyKey.Debug, false)
    if (!isEnabled) return
    val measures: List<Measure> = measureStorage.measures
    val reportDir = File(configuration.get(KopyKey.ReportPath) ?: "build/reports/kopy/", "metrics")
    val reportPath: String = reportDir.path
    val reportContent: String = buildString {
        var total: Duration = 0.milliseconds
        val sortedMeasures: List<Measure> =
            measures.sortedWith { a: Measure, b: Measure ->
                compareValuesBy(a = a, b = b, { it.key }, { -it.duration })
            }
        for ((key: String, measures: List<Measure>) in sortedMeasures.groupBy { it.key }) {
            val values: Sequence<Pair<String, Duration>> =
                measures.asSequence().map { measure ->
                    val value = measure.value

                    if (!measure.isNested) total = total + measure.duration

                    when (value) {
                        is FirElement -> value.render() to measure.duration
                        is IrElement -> value.dumpKotlinLike() to measure.duration
                        else -> value.toString() to measure.duration
                    }
                }
            val valuesToRender: String =
                values.map { "${it.first}, ${it.second}" }.toList().joinToString("\n") { "  - $it" }
            appendLine("$key:\n$valuesToRender")
        }
        appendLine()
        appendLine("TOTAL: $total")
    }
    if (reportPath.isBlank()) error("KopyReportPath must not be blank")
    val file: File =
        File(reportDir, "metrics_${now()}_${Uuid.random()}").apply {
            parentFile.mkdirs()
            createNewFile()
        }

    file.bufferedWriter().use {
        for (line: String in reportContent.lines()) {
            it.write(line)
            it.newLine()
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun now(): String = Clock.System.now().toString().replace(":", "-").replace(".", "-")
