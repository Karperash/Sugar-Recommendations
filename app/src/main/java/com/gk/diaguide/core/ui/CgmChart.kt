package com.gk.diaguide.core.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Info
import com.gk.diaguide.ui.theme.Primary
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun CgmLineChart(
    records: List<CgmRecord>,
    settings: UserSettings,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        if (records.isEmpty()) return@Canvas

        val paddingLeft = 52.dp.toPx()
        val paddingBottom = 28.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingRight = 12.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val minValue = minOf(records.minOf { it.glucoseValue }, settings.criticalLow) - 10
        val maxValue = maxOf(records.maxOf { it.glucoseValue }, settings.criticalHigh) + 10

        fun mapY(value: Double): Float {
            val normalized = ((value - minValue) / (maxValue - minValue)).toFloat().coerceIn(0f, 1f)
            return paddingTop + chartHeight - (normalized * chartHeight)
        }

        fun mapX(index: Int): Float {
            return if (records.size == 1) paddingLeft + chartWidth / 2
            else paddingLeft + chartWidth * index / records.lastIndex.toFloat()
        }

        val labelPaint = Paint().apply {
            color = Color.Gray.toArgb()
            textSize = 10.dp.toPx()
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))

        drawColoredZones(settings, minValue, maxValue, paddingLeft, paddingTop, chartWidth, chartHeight, ::mapY)

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
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect,
            )
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f".format(value),
                paddingLeft - 6.dp.toPx(),
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
            color = Primary,
            style = Stroke(width = 5f, cap = StrokeCap.Round),
        )

        records.zip(points).forEach { (record, point) ->
            val markerColor = when {
                record.glucoseValue <= settings.criticalLow || record.glucoseValue >= settings.criticalHigh -> Critical
                record.glucoseValue < settings.targetLow || record.glucoseValue > settings.targetHigh -> Warning
                record.meal || record.activity || record.insulin || record.symptom -> Info
                else -> Primary
            }
            drawCircle(color = markerColor, radius = 6.dp.toPx(), center = point)
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
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, paddingTop + chartHeight),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(paddingLeft, paddingTop + chartHeight),
            end = Offset(paddingLeft + chartWidth, paddingTop + chartHeight),
            strokeWidth = 1.dp.toPx(),
        )
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
