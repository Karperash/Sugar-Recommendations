package com.gk.diaguide.core.ui

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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Info
import com.gk.diaguide.ui.theme.Primary
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun CgmLineChart(
    records: List<CgmRecord>,
    settings: UserSettings,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember(records) { mutableIntStateOf(-1) }
    LaunchedEffect(records) {
        selectedIndex = -1
    }
    val density = LocalDensity.current
    val paddingLeftPx = with(density) { 52.dp.toPx() }
    val paddingRightPx = with(density) { 12.dp.toPx() }
    val lineColor = MaterialTheme.colorScheme.primary
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    if (records.isEmpty()) return@pointerInput
                    detectTapGestures { tap ->
                        val chartWidthPx = size.width - paddingLeftPx - paddingRightPx
                        if (tap.x < paddingLeftPx || tap.x > paddingLeftPx + chartWidthPx) {
                            selectedIndex = -1
                            return@detectTapGestures
                        }
                        val rel = ((tap.x - paddingLeftPx) / chartWidthPx).toDouble().coerceIn(0.0, 1.0)
                        val idx = if (records.size == 1) {
                            0
                        } else {
                            (rel * records.lastIndex).roundToInt().coerceIn(0, records.lastIndex)
                        }
                        selectedIndex = if (selectedIndex == idx) -1 else idx
                    }
                },
        ) {
            if (records.isEmpty()) return@Canvas

            val paddingBottom = 28.dp.toPx()
            val paddingTop = 8.dp.toPx()
            val paddingRightLocal = 12.dp.toPx()

            val chartWidth = size.width - paddingLeftPx - paddingRightLocal
            val chartHeight = size.height - paddingTop - paddingBottom

            val minValue = minOf(records.minOf { it.glucoseValue }, settings.criticalLow) - 10
            val maxValue = maxOf(records.maxOf { it.glucoseValue }, settings.criticalHigh) + 10

            fun mapY(value: Double): Float {
                val normalized = ((value - minValue) / (maxValue - minValue)).toFloat().coerceIn(0f, 1f)
                return paddingTop + chartHeight - (normalized * chartHeight)
            }

            fun mapX(index: Int): Float {
                return if (records.size == 1) paddingLeftPx + chartWidth / 2
                else paddingLeftPx + chartWidth * index / records.lastIndex.toFloat()
            }

            val labelPaint = Paint().apply {
                color = axisLabelColor.toArgb()
                textSize = 10.dp.toPx()
                isAntiAlias = true
                typeface = Typeface.DEFAULT
            }

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))

            drawColoredZones(settings, minValue, maxValue, paddingLeftPx, paddingTop, chartWidth, chartHeight, ::mapY)

            val thresholds = listOf(
                settings.criticalLow,
                settings.targetLow,
                settings.targetHigh,
                settings.criticalHigh,
            ).filter { it in minValue..maxValue }

            thresholds.forEach { value ->
                val y = mapY(value)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(paddingLeftPx, y),
                    end = Offset(paddingLeftPx + chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect,
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "%.0f".format(value),
                    paddingLeftPx - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT },
                )
            }

            val points = records.mapIndexed { index, _ ->
                Offset(mapX(index), mapY(records[index].glucoseValue))
            }

            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 5f, cap = StrokeCap.Round),
            )

            records.zip(points).forEachIndexed { index, (record, point) ->
                val markerColor = when {
                    index == selectedIndex -> lineColor
                    record.glucoseValue <= settings.criticalLow || record.glucoseValue >= settings.criticalHigh -> Critical
                    record.glucoseValue < settings.targetLow || record.glucoseValue > settings.targetHigh -> Warning
                    record.meal || record.activity || record.insulin || record.symptom -> Info
                    else -> Primary
                }
                val radius = if (index == selectedIndex) 9.dp.toPx() else 6.dp.toPx()
                drawCircle(color = markerColor, radius = radius, center = point)
            }

            val zone = ZoneId.systemDefault()
            val step = maxOf(1, records.size / 5)
            records.forEachIndexed { index, record ->
                if (index % step == 0 || index == records.lastIndex) {
                    val x = mapX(index)
                    val label = record.timestamp.atZone(zone).format(timeFormatter)

                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x, paddingTop + chartHeight),
                        end = Offset(x, paddingTop + chartHeight + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        paddingTop + chartHeight + 18.dp.toPx(),
                        Paint(labelPaint).apply { textAlign = Paint.Align.CENTER },
                    )
                }
            }

            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(paddingLeftPx, paddingTop),
                end = Offset(paddingLeftPx, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(paddingLeftPx, paddingTop + chartHeight),
                end = Offset(paddingLeftPx + chartWidth, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )
        }

        if (selectedIndex in records.indices) {
            val r = records[selectedIndex]
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 32.dp)
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

private fun DrawScope.drawColoredZones(
    settings: UserSettings,
    minValue: Double,
    maxValue: Double,
    paddingLeft: Float,
    paddingTop: Float,
    chartWidth: Float,
    chartHeight: Float,
    mapY: (Double) -> Float,
) {
    fun zoneRect(top: Double, bottom: Double, color: Color) {
        val clampedTop = top.coerceIn(minValue, maxValue)
        val clampedBottom = bottom.coerceIn(minValue, maxValue)
        val yTop = mapY(clampedTop)
        val yBottom = mapY(clampedBottom)
        if (yBottom > yTop) {
            drawRect(
                color = color,
                topLeft = Offset(paddingLeft, yTop),
                size = Size(chartWidth, yBottom - yTop),
            )
        }
    }

    zoneRect(maxValue, settings.criticalHigh, Critical.copy(alpha = 0.06f))
    zoneRect(settings.criticalHigh, settings.targetHigh, Warning.copy(alpha = 0.06f))
    zoneRect(settings.targetHigh, settings.targetLow, Success.copy(alpha = 0.10f))
    zoneRect(settings.targetLow, settings.criticalLow, Warning.copy(alpha = 0.06f))
    zoneRect(settings.criticalLow, minValue, Critical.copy(alpha = 0.06f))
}
