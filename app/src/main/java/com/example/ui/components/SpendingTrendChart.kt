package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendlyCoral
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import com.example.ui.viewmodel.MonthSpendPoint

@Composable
fun SpendingTrendChart(
  dataPoints: List<MonthSpendPoint>,
  capAmount: Double = 2500.0,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "6-Month Spending Trend",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          Text(
            text = "May – October 2024",
            fontSize = 12.sp,
            color = SpendlyOnSurfaceVariant
          )
        }

        // Cap indicator pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF1F2))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "— Cap $${capAmount.toInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyCoral
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Canvas for Trend Curve
      val maxVal = (dataPoints.maxOfOrNull { it.amount } ?: 3000.0).coerceAtLeast(capAmount * 1.25)
      val minVal = (dataPoints.minOfOrNull { it.amount } ?: 1500.0).coerceAtMost(1000.0)
      val range = (maxVal - minVal).coerceAtLeast(500.0)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        Canvas(modifier = Modifier.matchParentSize()) {
          val width = size.width
          val height = size.height

          // Draw dashed cap line
          val capY = (height - ((capAmount - minVal) / range * height)).toFloat().coerceIn(10f, height - 10f)
          drawLine(
            color = Color(0xFFF43F5E).copy(alpha = 0.6f),
            start = Offset(0f, capY),
            end = Offset(width, capY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
          )

          if (dataPoints.size >= 2) {
            val stepX = width / (dataPoints.size - 1)
            val points = dataPoints.mapIndexed { index, item ->
              val x = index * stepX
              val y = (height - ((item.amount - minVal) / range * (height - 30f)) - 15f).toFloat()
              Offset(x, y)
            }

            // Build smooth cubic bezier path
            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
              val p0 = points[i]
              val p1 = points[i + 1]
              val cx = (p0.x + p1.x) / 2
              path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
              fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw area gradient
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  SpendlyPrimary.copy(alpha = 0.25f),
                  SpendlyPrimary.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = height
              )
            )

            // Draw line curve
            drawPath(
              path = path,
              color = SpendlyPrimary,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw point dots
            points.forEachIndexed { idx, pt ->
              val isOverCap = dataPoints[idx].amount > capAmount
              val dotBorderColor = if (isOverCap) SpendlyCoral else SpendlyPrimary

              drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = pt
              )
              drawCircle(
                color = dotBorderColor,
                radius = 5.dp.toPx(),
                center = pt,
                style = Stroke(width = 2.5.dp.toPx())
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom Month Labels & figures
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        dataPoints.forEach { pt ->
          val isOverCap = pt.amount > capAmount
          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = pt.label,
              fontSize = 11.sp,
              fontWeight = if (pt.isCurrent) FontWeight.Bold else FontWeight.Medium,
              color = when {
                pt.isCurrent -> SpendlyPrimary
                isOverCap -> SpendlyCoral
                else -> SpendlyOnSurfaceVariant
              }
            )
            Text(
              text = "$${String.format("%.1fk", pt.amount / 1000.0)}",
              fontSize = 11.sp,
              fontWeight = if (pt.isCurrent || isOverCap) FontWeight.Bold else FontWeight.Normal,
              color = when {
                pt.isCurrent -> SpendlyPrimary
                isOverCap -> SpendlyCoral
                else -> SpendlyOnSurface
              }
            )
          }
        }
      }
    }
  }
}
