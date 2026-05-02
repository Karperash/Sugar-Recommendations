package com.gk.diaguide.presentation.chart

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.core.util.formatGlucose
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Info
import com.gk.diaguide.ui.theme.Primary
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Линейный график глюкозы по времени: шкала Y от реальных значений с полями,
 * подписи осей в единицах настроек, без «ломаной» смеси диапазонов.
 */
@Composable
fun GlucoseSeriesChart(
    records: List<CgmRecord>,
    settings: UserSettings,
    modifier: Modifier = Modifier,
    /** Если false — не перехватывать касания (например мини-график на главной под `clickable`). */
    pointerEnabled: Boolean = true,
) {
    val sorted = remember(records) { records.sortedBy { it.timestamp } }
    var selectedIndex by remember(sorted) { mutableIntStateOf(-1) }
    LaunchedEffect(sorted) {
        selectedIndex = -1
    }

    val density = LocalDensity.current
    val paddingLeftPx = with(density) { 56.dp.toPx() }
    val paddingRightPx = with(density) { 12.dp.toPx() }
    val lineColor = MaterialTheme.colorScheme.primary
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxWidth()) {
        val canvasModifier = Modifier.fillMaxSize().then(
            if (pointerEnabled) {
                Modifier.pointerInput(sorted) {
                    if (sorted.isEmpty()) return@pointerInput
                    detectTapGestures { tap ->
                        val chartWidthPx = size.width - paddingLeftPx - paddingRightPx
                        if (tap.x < paddingLeftPx || tap.x > paddingLeftPx + chartWidthPx) {
                            selectedIndex = -1
                            return@detectTapGestures
                        }
                        val tStart = sorted.first().timestamp.toEpochMilli()
                        val tEnd = sorted.last().timestamp.toEpochMilli()
                        val rel = ((tap.x - paddingLeftPx) / chartWidthPx).toDouble().coerceIn(0.0, 1.0)
                        val nearest = if (sorted.size == 1) {
                            0
                        } else {
                            val targetEpoch = tStart + (rel * (tEnd - tStart).toDouble()).toLong()
                            sorted.indices.minByOrNull { idx ->
                                abs(sorted[idx].timestamp.toEpochMilli() - targetEpoch)
                            } ?: 0
                        }
                        selectedIndex = if (selectedIndex == nearest) -1 else nearest
                    }
                }
            } else {
                Modifier
            },
        )
        Canvas(modifier = canvasModifier) {
            if (sorted.isEmpty()) return@Canvas

            val paddingBottom = 32.dp.toPx()
            val paddingTop = 10.dp.toPx()
            val paddingRightLocal = 12.dp.toPx()
            val chartWidth = size.width - paddingLeftPx - paddingRightLocal
            val chartHeight = size.height - paddingTop - paddingBottom

            val values = sorted.map { it.glucoseValue }
            val vMin = values.minOrNull()!!
            val vMax = values.maxOrNull()!!
            val minSpan = if (settings.glucoseUnit == GlucoseUnit.MG_DL) 18.0 else 1.0
            val spanRaw = (vMax - vMin).coerceAtLeast(minSpan)
            val pad = spanRaw * 0.12
            var yMin = vMin - pad
            var yMax = vMax + pad
            if (yMax - yMin < minSpan) {
                val mid = (vMin + vMax) / 2.0
                yMin = mid - minSpan / 2.0
                yMax = mid + minSpan / 2.0
            }

            fun mapY(value: Double): Float {
                val t = ((value - yMin) / (yMax - yMin)).toFloat().coerceIn(0f, 1f)
                return paddingTop + chartHeight - t * chartHeight
            }

            val tStart = sorted.first().timestamp.toEpochMilli()
            val tEnd = sorted.last().timestamp.toEpochMilli()
            fun mapX(record: CgmRecord): Float {
                val epochMs = record.timestamp.toEpochMilli()
                return if (tEnd == tStart) {
                    paddingLeftPx + chartWidth / 2f
                } else {
                    val r = (epochMs - tStart).toDouble() / (tEnd - tStart).toDouble()
                    paddingLeftPx + chartWidth * r.toFloat().coerceIn(0f, 1f)
                }
            }

            val labelPaint = Paint().apply {
                color = axisLabelColor.toArgb()
                textSize = 10.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.DEFAULT
            }

            // Целевой коридор (только если пересекается с видимым диапазоном)
            val tgtLowVis = max(yMin, settings.targetLow)
            val tgtHighVis = min(yMax, settings.targetHigh)
            if (tgtLowVis < tgtHighVis) {
                drawRect(
                    color = Success.copy(alpha = 0.12f),
                    topLeft = Offset(paddingLeftPx, mapY(tgtHighVis)),
                    size = Size(chartWidth, mapY(tgtLowVis) - mapY(tgtHighVis)),
                )
            }

            val thresholds = listOf(
                settings.criticalLow,
                settings.targetLow,
                settings.targetHigh,
                settings.criticalHigh,
            ).filter { it in yMin..yMax }

            thresholds.forEach { value ->
                val y = mapY(value)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.35f),
                    start = Offset(paddingLeftPx, y),
                    end = Offset(paddingLeftPx + chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formatAxisTick(value, settings.glucoseUnit),
                    paddingLeftPx - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT },
                )
            }

            val points = sorted.map { r -> Offset(mapX(r), mapY(r.glucoseValue)) }
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round),
            )

            sorted.zip(points).forEachIndexed { index, (record, point) ->
                val markerColor = when {
                    index == selectedIndex -> lineColor
                    record.glucoseValue <= settings.criticalLow || record.glucoseValue >= settings.criticalHigh -> Critical
                    record.glucoseValue < settings.targetLow || record.glucoseValue > settings.targetHigh -> Warning
                    record.meal || record.activity || record.insulin || record.symptom -> Info
                    else -> Primary
                }
                val radius = if (index == selectedIndex) 8.dp.toPx() else 5.dp.toPx()
                drawCircle(color = markerColor, radius = radius, center = point)
            }

            val zone = ZoneId.systemDefault()
            val step = max(1, sorted.size / 5)
            sorted.forEachIndexed { index, record ->
                if (index % step == 0 || index == sorted.lastIndex) {
                    val x = mapX(record)
                    val label = record.timestamp.atZone(zone).format(timeFormatter)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        paddingTop + chartHeight + 18.dp.toPx(),
                        Paint(labelPaint).apply { textAlign = Paint.Align.CENTER },
                    )
                }
            }

            drawLine(
                color = Color.Gray.copy(alpha = 0.55f),
                start = Offset(paddingLeftPx, paddingTop),
                end = Offset(paddingLeftPx, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.55f),
                start = Offset(paddingLeftPx, paddingTop + chartHeight),
                end = Offset(paddingLeftPx + chartWidth, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )
        }

        if (selectedIndex in sorted.indices) {
            val r = sorted[selectedIndex]
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 28.dp)
                    .fillMaxWidth(0.94f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.chart_point_tooltip,
                        r.glucoseValue.formatGlucose(settings.glucoseUnit),
                        r.timestamp.formatDateTime(),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun formatAxisTick(value: Double, unit: GlucoseUnit): String =
    if (unit == GlucoseUnit.MG_DL) "%.0f".format(Locale.US, value) else "%.1f".format(Locale.US, value)
