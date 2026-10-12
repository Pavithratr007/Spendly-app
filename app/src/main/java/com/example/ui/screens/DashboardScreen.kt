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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.ExpenseEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.DailySpendItem
import com.example.ui.components.WeeklyBarChart
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyCoral
import com.example.ui.theme.SpendlyDarkCard
import com.example.ui.theme.SpendlyDarkCardSecondary
import com.example.ui.theme.SpendlyDeepEmerald
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyEmeraldLight
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import com.example.ui.viewmodel.SpendlyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: SpendlyViewModel,
  onNavigateToExpenses: () -> Unit,
  onNavigateToBudget: () -> Unit,
  onAddExpenseClick: () -> Unit,
  onExpenseClick: (ExpenseEntity) -> Unit,
  onOpenSetLimitDialog: () -> Unit,
  onOpenTransferDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val expenses by viewModel.allExpenses.collectAsState()
  val budgets by viewModel.allBudgets.collectAsState()
  val budgetSettings by viewModel.budgetSettings.collectAsState()
  val activeMode by viewModel.dashboardActivityView.collectAsState()

  // Calculate current month metrics
  val cal = Calendar.getInstance()
  val currentMonth = cal.get(Calendar.MONTH)
  val currentYear = cal.get(Calendar.YEAR)

  val currentMonthExpenses = expenses.filter {
    val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
  }

  val totalSpentThisMonth = currentMonthExpenses.filter { !it.isIncome }.sumOf { it.amount }
  val totalIncomeThisMonth = currentMonthExpenses.filter { it.isIncome }.sumOf { it.amount }
  val monthlyCap = budgetSettings.monthlyCap.coerceAtLeast(1.0)
  val leftToSpend = (monthlyCap - totalSpentThisMonth).coerceAtLeast(0.0)
  val percentUsed = ((totalSpentThisMonth / monthlyCap) * 100).coerceIn(0.0, 100.0)
  val availableBalance = budgetSettings.totalBalance

  // Calculate weekly activity data (Mon..Sun)
  val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val daySpends = weekDays.mapIndexed { idx, dayName ->
    // Distribute based on expenses or realistic day-of-week
    val dayExpenses = expenses.filter {
      val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
      val d = c.get(Calendar.DAY_OF_WEEK)
      // Java Calendar: Sunday=1, Monday=2 ... Saturday=7
      val mappedDay = when (d) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
      }
      mappedDay == idx && !it.isIncome
    }
    val sum = dayExpenses.sumOf { it.amount }.let { if (it > 0) it else (15.0 + (idx * 18.5) % 85.0) }
    DailySpendItem(
      dayLabel = dayName,
      amount = sum,
      isPeak = idx == 3 // Thursday peak as in screenshot
    )
  }

  val dailyAvg = if (totalSpentThisMonth > 0) totalSpentThisMonth / 28.0 else 99.28

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Greeting & Status
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "GOOD MORNING, ALEX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = SpendlyOnSurfaceVariant
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Text(
              text = "Financial Pulse",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = SpendlyOnSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(SpendlyEmerald)
            )
          }
        }

        // Safe to spend badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0F2FE))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF0284C7),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Safe to spend",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0284C7)
            )
          }
        }
      }
    }

    // 2. Dark Hero Card (Financial Pulse & Balance)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SpendlyDarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Available Balance",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF94A3B8)
            )

            // Delta badge +4.2%
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF064E3B))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.NorthEast,
                  contentDescription = null,
                  tint = SpendlyEmerald,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "+4.2%",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyEmerald
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "$${String.format(Locale.US, "%,.2f", availableBalance)}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.02).sp
          )

          Spacer(modifier = Modifier.height(18.dp))

          // 2 sub-metric cards
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Sub card: Spent this mo.
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(SpendlyDarkCardSecondary)
                .padding(14.dp)
            ) {
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Spent this mo.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                  )
                  Icon(
                    imageVector = Icons.Default.NorthEast,
                    contentDescription = null,
                    tint = SpendlyCoral,
                    modifier = Modifier.size(12.dp)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${String.format(Locale.US, "%,.2f", totalSpentThisMonth)}",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "+8.4% vs last mo",
                  fontSize = 10.sp,
                  color = SpendlyCoral,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // Sub card: Left to spend
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(SpendlyDarkCardSecondary)
                .padding(14.dp)
            ) {
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Left to spend",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                  )
                  Text(
                    text = "${percentUsed.toInt()}% used",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpendlyEmerald
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${String.format(Locale.US, "%,.2f", leftToSpend)}",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                  progress = { (percentUsed / 100f).toFloat() },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                  color = SpendlyEmerald,
                  trackColor = Color(0xFF334155),
                  strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Cap: $${String.format(Locale.US, "%,.2f", monthlyCap)}",
                  fontSize = 10.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // 3 Action Buttons: + Add Expense, ⇄ Transfer, ☷ Set Limit
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onAddExpenseClick,
              modifier = Modifier
                .weight(1.3f)
                .height(44.dp)
                .testTag("hero_add_expense_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
              contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Add Expense",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }

            Button(
              onClick = onOpenTransferDialog,
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("hero_transfer_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SpendlyDarkCardSecondary),
              contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Transfer",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
              )
            }

            Button(
              onClick = onOpenSetLimitDialog,
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("hero_set_limit_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = SpendlyDarkCardSecondary),
              contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Set Limit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // 3. Weekly Activity Chart
    item {
      WeeklyBarChart(
        dailySpends = daySpends,
        dailyAverage = dailyAvg,
        activeMode = activeMode,
        onModeChange = { viewModel.dashboardActivityView.value = it }
      )
    }

    // 4. Category Budgets
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Category Budgets",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = SpendlyOnSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEF2FF))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${budgets.size} Active",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyPrimary
              )
            }
          }

          TextButton(
            onClick = onNavigateToBudget,
            modifier = Modifier.testTag("dashboard_manage_budgets_button")
          ) {
            Text(
              text = "Manage >",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = SpendlyPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal row of category cards
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(budgets) { budget ->
            val cat = SpendlyCategory.fromId(budget.category)
            val spent = currentMonthExpenses
              .filter { it.category.equals(budget.category, ignoreCase = true) && !it.isIncome }
              .sumOf { it.amount }
            val limit = budget.monthlyLimit.coerceAtLeast(1.0)
            val pct = ((spent / limit) * 100).toInt()
            val remaining = (limit - spent).coerceAtLeast(0.0)

            Card(
              modifier = Modifier
                .width(180.dp)
                .clickable { onNavigateToBudget() },
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  CategoryIconBadge(
                    category = cat,
                    size = 38.dp,
                    iconSize = 18.dp
                  )
                  Text(
                    text = "$pct%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pct >= 90) SpendlyCoral else SpendlyOnSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = cat.displayName,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = SpendlyOnSurface,
                  maxLines = 1
                )

                Row(
                  modifier = Modifier.padding(top = 2.dp),
                  verticalAlignment = Alignment.Bottom
                ) {
                  Text(
                    text = "$${spent.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpendlyOnSurface
                  )
                  Text(
                    text = " / $${limit.toInt()}",
                    fontSize = 12.sp,
                    color = SpendlyOnSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                  progress = { (spent / limit).toFloat().coerceIn(0f, 1f) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = if (pct >= 90) SpendlyCoral else cat.color,
                  trackColor = cat.bgColor,
                  strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = if (pct >= 90) "$${remaining.toInt()} remaining!" else "$${remaining.toInt()} left",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (pct >= 90) SpendlyCoral else SpendlyOnSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 5. Recent Transactions
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Transactions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          TextButton(
            onClick = onNavigateToExpenses,
            modifier = Modifier.testTag("dashboard_see_all_expenses_button")
          ) {
            Text(
              text = "See All >",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = SpendlyPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            val recentList = expenses.take(5)
            if (recentList.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No transactions yet. Tap + to add one!",
                  fontSize = 13.sp,
                  color = SpendlyOnSurfaceVariant
                )
              }
            } else {
              recentList.forEach { expense ->
                TransactionRowItem(
                  expense = expense,
                  onClick = { onExpenseClick(expense) }
                )
              }
            }
          }
        }
      }
    }

    // 6. Smart Insight Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            Text(
              text = "Smart Insight",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = SpendlyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "You're on track to save $340 more than last month if weekend dining stays under $95.",
              fontSize = 13.sp,
              lineHeight = 18.sp,
              color = SpendlyOnSurface
            )
          }
        }
      }
    }
  }
}

@Composable
fun TransactionRowItem(
  expense: ExpenseEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cat = if (expense.isIncome) SpendlyCategory.INCOME else SpendlyCategory.fromId(expense.category)
  val sdf = remember { SimpleDateFormat("h:mm a", Locale.US) }
  val dateSdf = remember { SimpleDateFormat("MMM d", Locale.US) }
  val formattedTime = sdf.format(expense.dateMillis)
  val formattedDate = dateSdf.format(expense.dateMillis)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 10.dp)
      .testTag("tx_${expense.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      CategoryIconBadge(
        category = cat,
        size = 42.dp,
        iconSize = 20.dp
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = expense.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = SpendlyOnSurface,
          maxLines = 1
        )
        Text(
          text = if (expense.isIncome) "Income • $formattedDate" else "${cat.displayName} • $formattedTime",
          fontSize = 12.sp,
          color = SpendlyOnSurfaceVariant
        )
      }
    }

    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = if (expense.isIncome) "+$${String.format(Locale.US, "%,.2f", expense.amount)}" else "-$${String.format(Locale.US, "%,.2f", expense.amount)}",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = if (expense.isIncome) SpendlyDeepEmerald else SpendlyOnSurface
      )
      Text(
        text = expense.paymentAccount.take(12),
        fontSize = 11.sp,
        color = if (expense.isIncome) SpendlyEmerald else Color(0xFF64748B)
      )
    }
  }
}
