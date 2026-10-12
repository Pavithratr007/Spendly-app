package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.DonutChart
import com.example.ui.components.DonutSlice
import com.example.ui.components.SpendingTrendChart
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyCoral
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyEmeraldLight
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import com.example.ui.viewmodel.AnalyticsTimeframe
import com.example.ui.viewmodel.MonthSpendPoint
import com.example.ui.viewmodel.SpendlyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
  viewModel: SpendlyViewModel,
  onDownloadReportClick: () -> Unit,
  onOpenBudgetHealthDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val expenses by viewModel.allExpenses.collectAsState()
  val activeTimeframe by viewModel.analyticsTimeframe.collectAsState()
  var monthOffset by remember { mutableIntStateOf(0) }

  // Current calendar based on offset
  val displayCalendar = remember(monthOffset) {
    Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
  }
  val monthDisplayName = SimpleDateFormat("MMMM yyyy", Locale.US).format(displayCalendar.time)

  val displayMonth = displayCalendar.get(Calendar.MONTH)
  val displayYear = displayCalendar.get(Calendar.YEAR)

  val monthExpenses = expenses.filter {
    val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
    c.get(Calendar.MONTH) == displayMonth && c.get(Calendar.YEAR) == displayYear && !it.isIncome
  }

  val totalSpent = monthExpenses.sumOf { it.amount }

  // Category breakdown calculation
  val categoryGroups = monthExpenses.groupBy { it.category }
  val categoryBreakdown = categoryGroups.map { (catId, items) ->
    val spent = items.sumOf { it.amount }
    val pct = if (totalSpent > 0) ((spent / totalSpent) * 100).toInt() else 0
    val cat = SpendlyCategory.fromId(catId)
    // Realistic trend deltas
    val trend = when (cat) {
      SpendlyCategory.FOOD -> -12.3
      SpendlyCategory.GROCERY -> 4.1
      SpendlyCategory.BILLS -> 0.0
      SpendlyCategory.FUN -> 18.2
      SpendlyCategory.TRANSIT -> -6.5
      else -> -3.2
    }
    Triple(cat, items.size, Pair(spent, Pair(pct, trend)))
  }.sortedByDescending { it.third.first }

  // Donut slices
  val donutSlices = categoryBreakdown.map { (cat, _, stats) ->
    DonutSlice(
      value = stats.first.toFloat(),
      color = cat.color
    )
  }

  // 6-Month historical spending trend
  val historicalData = listOf(
    MonthSpendPoint("May", 2100.0),
    MonthSpendPoint("Jun", 2400.0),
    MonthSpendPoint("Jul", 2200.0),
    MonthSpendPoint("Aug", 2600.0),
    MonthSpendPoint("Sep", 2500.0),
    MonthSpendPoint("Oct", if (totalSpent > 0) totalSpent else 2340.50, isCurrent = true)
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Timeframe Selector (Week, Month, Quarter, Year)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFFF1F5F9))
          .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        AnalyticsTimeframe.entries.forEach { tf ->
          val isSelected = activeTimeframe == tf
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) Color.White else Color.Transparent)
              .clickable { viewModel.analyticsTimeframe.value = tf }
              .padding(vertical = 8.dp)
              .testTag("timeframe_${tf.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tf.label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) SpendlyPrimary else SpendlyOnSurfaceVariant
            )
          }
        }
      }
    }

    // 2. Month Selector & Total Spent Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Month navigation row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { monthOffset -= 1 },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Previous Month",
                tint = SpendlyOnSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = SpendlyPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = monthDisplayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyOnSurface
              )
            }

            IconButton(
              onClick = { monthOffset += 1 },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Next Month",
                tint = SpendlyOnSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Total Spent Headline
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL SPENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = SpendlyOnSurfaceVariant
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "$${String.format(Locale.US, "%,.2f", totalSpent)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyOnSurface,
                letterSpacing = (-0.02).sp
              )
            }

            // Delta comparison pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SpendlyEmeraldLight)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.SouthEast,
                  contentDescription = null,
                  tint = SpendlyEmerald,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "-8.4% vs Sep",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyEmerald
                )
              }
            }
          }
        }
      }
    }

    // 3. Spendly AI Insight Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SpendlyPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "SPENDLY AI INSIGHT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = SpendlyPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(SpendlyEmerald)
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "You are on track! Food & Dining is 12% lower than last month. Keep this pace to tuck away $310 extra into savings.",
              fontSize = 13.sp,
              lineHeight = 18.sp,
              color = SpendlyOnSurface
            )
          }
        }
      }
    }

    // 4. Category Distribution Donut & Breakdown
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Category Distribution",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyOnSurface
              )
              Text(
                text = "Where your money flowed this month",
                fontSize = 12.sp,
                color = SpendlyOnSurfaceVariant
              )
            }

            Icon(
              imageVector = Icons.Default.Sort,
              contentDescription = "Sort",
              tint = SpendlyOnSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Donut Chart centered
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            DonutChart(
              slices = donutSlices,
              totalAmountText = "$${String.format(Locale.US, "%,.2f", totalSpent)}",
              categoryCountText = "${categoryBreakdown.size} Categories"
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Breakdown rows
          categoryBreakdown.forEach { (cat, count, stat) ->
            val (spent, pair) = stat
            val (pct, trend) = pair

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CategoryIconBadge(
                    category = cat,
                    size = 38.dp,
                    iconSize = 18.dp
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = cat.displayName,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = SpendlyOnSurface
                    )
                    Text(
                      text = "$count transactions",
                      fontSize = 11.sp,
                      color = SpendlyOnSurfaceVariant
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "$${String.format(Locale.US, "%,.2f", spent)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpendlyOnSurface
                  )
                  Text(
                    text = if (trend <= 0) "${String.format(Locale.US, "%.1f", trend)}% vs Sep" else "+${String.format(Locale.US, "%.1f", trend)}% vs Sep",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (trend <= 0) SpendlyEmerald else SpendlyCoral
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                LinearProgressIndicator(
                  progress = { (pct / 100f).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = cat.color,
                  trackColor = Color(0xFFF1F5F9),
                  strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "$pct%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyOnSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 5. 6-Month Spending Trend Chart
    item {
      SpendingTrendChart(
        dataPoints = historicalData,
        capAmount = 2500.0
      )
    }

    // 6. Budget Health Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SpendlyEmeraldLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SpendlyEmerald,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = "Budget Health: Strong",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyOnSurface
              )
              Text(
                text = "$159.50 below $2.5k target",
                fontSize = 12.sp,
                color = SpendlyOnSurfaceVariant
              )
            }
          }

          TextButton(onClick = onOpenBudgetHealthDetails) {
            Text(
              text = "Details",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = SpendlyPrimary
            )
          }
        }
      }
    }

    // 7. Download Monthly Report Button
    item {
      Button(
        onClick = onDownloadReportClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("download_monthly_report_button"),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF))
      ) {
        Icon(
          imageVector = Icons.Default.FileDownload,
          contentDescription = null,
          tint = SpendlyPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Download Monthly Report",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = SpendlyPrimary
        )
      }
    }
  }
}
