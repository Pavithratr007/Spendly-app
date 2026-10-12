package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant

data class DonutSlice(
  val value: Float,
  val color: Color
)

@Composable
fun DonutChart(
  slices: List<DonutSlice>,
  totalAmountText: String,
  categoryCountText: String,
  modifier: Modifier = Modifier,
  size: Dp = 190.dp,
  strokeWidth: Dp = 22.dp
) {
  val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      var startAngle = -90f
      val strokePx = strokeWidth.toPx()

      if (slices.isEmpty()) {
        drawCircle(
          color = Color(0xFFE2E8F0),
          style = Stroke(width = strokePx)
        )
      } else {
        slices.forEach { slice ->
          val sweepAngle = (slice.value / total) * 360f
          // Add small gap between slices
          val gapAngle = if (slices.size > 1) 3f else 0f
          val adjustedSweep = (sweepAngle - gapAngle).coerceAtLeast(1f)

          drawArc(
            color = slice.color,
            startAngle = startAngle + (gapAngle / 2f),
            sweepAngle = adjustedSweep,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
          )
          startAngle += sweepAngle
        }
      }
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "TOTAL",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = SpendlyOnSurfaceVariant
      )
      Text(
        text = totalAmountText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = SpendlyOnSurface,
        letterSpacing = (-0.02).sp
      )
      Text(
        text = categoryCountText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = SpendlyEmerald
      )
    }
  }
}
