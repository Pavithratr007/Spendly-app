package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyEmeraldLight
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import java.util.Locale

data class DailySpendItem(
  val dayLabel: String,
  val amount: Double,
  val isPeak: Boolean = false
)

@Composable
fun WeeklyBarChart(
  dailySpends: List<DailySpendItem>,
  dailyAverage: Double,
  activeMode: String,
  onModeChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedDayIndex by remember { mutableIntStateOf(-1) }

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
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Weekly Activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = "Daily avg: ",
              fontSize = 12.sp,
              color = SpendlyOnSurfaceVariant
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SpendlyEmeraldLight)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$${String.format(Locale.US, "%.2f", dailyAverage)}/day",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyEmerald
              )
            }
          }
        }

        // Segmented Toggle: Week / Month
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .padding(3.dp)
        ) {
          listOf("Week", "Month").forEach { mode ->
            val isSelected = activeMode == mode
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (isSelected) Color.White else Color.Transparent)
                .clickable { onModeChange(mode) }
                .padding(horizontal = 10.dp, vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = mode,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) SpendlyPrimary else SpendlyOnSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Bar Chart container
      val maxAmount = (dailySpends.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        dailySpends.forEachIndexed { index, item ->
          val fraction = (item.amount / maxAmount).toFloat().coerceIn(0.12f, 1f)
          val animatedHeight by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 600),
            label = "bar_anim"
          )
          val isSelected = selectedDayIndex == index
          val isPeak = item.isPeak

          Column(
            modifier = Modifier
              .weight(1f)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                selectedDayIndex = if (selectedDayIndex == index) -1 else index
              }
              .testTag("bar_${item.dayLabel.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
          ) {
            // Peak indicator badge
            if (isPeak) {
              Box(
                modifier = Modifier
                  .offset(y = (-4).dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(SpendlyPrimary)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Peak",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            } else if (isSelected) {
              Box(
                modifier = Modifier
                  .offset(y = (-4).dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF1E293B))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "$${item.amount.toInt()}",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            } else {
              Spacer(modifier = Modifier.height(16.dp))
            }

            // The Bar itself
            Box(
              modifier = Modifier
                .width(26.dp)
                .fillMaxHeight(animatedHeight * 0.75f)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                  when {
                    isPeak -> SpendlyPrimary
                    isSelected -> Color(0xFF6366F1)
                    else -> Color(0xFFE0E7FF)
                  }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Day label
            Text(
              text = item.dayLabel,
              fontSize = 11.sp,
              fontWeight = if (isPeak || isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isPeak) SpendlyPrimary else SpendlyOnSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = if (selectedDayIndex >= 0 && selectedDayIndex < dailySpends.size) {
          "${dailySpends[selectedDayIndex].dayLabel}: $${String.format(Locale.US, "%.2f", dailySpends[selectedDayIndex].amount)} spent"
        } else {
          "Tip: Tap any bar to view daily breakdown"
        },
        fontSize = 11.sp,
        color = SpendlyOnSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }
  }
}
