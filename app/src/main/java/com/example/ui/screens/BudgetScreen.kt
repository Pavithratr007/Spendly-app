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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryBudgetEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyCoral
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyEmeraldLight
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import com.example.ui.viewmodel.SpendlyViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun BudgetScreen(
  viewModel: SpendlyViewModel,
  onEditMonthlyCap: () -> Unit,
  onEditCategoryBudget: (CategoryBudgetEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val expenses by viewModel.allExpenses.collectAsState()
  val budgets by viewModel.allBudgets.collectAsState()
  val budgetSettings by viewModel.budgetSettings.collectAsState()

  // Calculate current month's spent total
  val cal = Calendar.getInstance()
  val currentMonth = cal.get(Calendar.MONTH)
  val currentYear = cal.get(Calendar.YEAR)

  val currentMonthExpenses = expenses.filter {
    val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear && !it.isIncome
  }

  val totalSpent = currentMonthExpenses.sumOf { it.amount }
  val monthlyCap = budgetSettings.monthlyCap.coerceAtLeast(1.0)
  val remainingBudget = (monthlyCap - totalSpent).coerceAtLeast(0.0)
  val percentCapUsed = ((totalSpent / monthlyCap) * 100).toInt()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Overall Monthly Budget Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Savings,
                  contentDescription = null,
                  tint = SpendlyPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Monthly Spending Cap",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = SpendlyOnSurfaceVariant
                )
                Text(
                  text = "$${String.format(Locale.US, "%,.2f", monthlyCap)}",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyOnSurface
                )
              }
            }

            IconButton(
              onClick = onEditMonthlyCap,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .testTag("edit_monthly_cap_button")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Monthly Budget",
                tint = SpendlyPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Progress bar
          LinearProgressIndicator(
            progress = { (totalSpent / monthlyCap).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp)),
            color = if (percentCapUsed >= 90) SpendlyCoral else SpendlyPrimary,
            trackColor = Color(0xFFF1F5F9),
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Spent this month",
                fontSize = 11.sp,
                color = SpendlyOnSurfaceVariant
              )
              Text(
                text = "$${String.format(Locale.US, "%,.2f", totalSpent)} ($percentCapUsed%)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (percentCapUsed >= 90) SpendlyCoral else SpendlyOnSurface
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Remaining allowance",
                fontSize = 11.sp,
                color = SpendlyOnSurfaceVariant
              )
              Text(
                text = "$${String.format(Locale.US, "%,.2f", remainingBudget)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyEmerald
              )
            }
          }
        }
      }
    }

    // 2. Category Budgets Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Category Allowances",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          Text(
            text = "Set limits for specific spending areas",
            fontSize = 12.sp,
            color = SpendlyOnSurfaceVariant
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEF2FF))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${budgets.size} Active",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyPrimary
          )
        }
      }
    }

    // 3. Category Budgets List
    items(budgets) { budget ->
      val cat = SpendlyCategory.fromId(budget.category)
      val spent = currentMonthExpenses
        .filter { it.category.equals(budget.category, ignoreCase = true) }
        .sumOf { it.amount }
      val limit = budget.monthlyLimit.coerceAtLeast(1.0)
      val pct = ((spent / limit) * 100).toInt()
      val remaining = (limit - spent).coerceAtLeast(0.0)
      val isNearOrOver = pct >= 90

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onEditCategoryBudget(budget) }
          .testTag("budget_category_${cat.id.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              CategoryIconBadge(
                category = cat,
                size = 42.dp,
                iconSize = 20.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = cat.displayName,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = SpendlyOnSurface
                )
                Text(
                  text = "$${spent.toInt()} spent of $${limit.toInt()}",
                  fontSize = 12.sp,
                  color = SpendlyOnSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              if (isNearOrOver) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = "Warning",
                  tint = SpendlyCoral,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }

              Text(
                text = "$pct%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNearOrOver) SpendlyCoral else SpendlyOnSurface
              )

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = { onEditCategoryBudget(budget) },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit",
                  tint = SpendlyPrimary,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = { (spent / limit).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (isNearOrOver) SpendlyCoral else cat.color,
            trackColor = cat.bgColor,
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (isNearOrOver) "Alert: Close to limit!" else "Healthy pacing",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = if (isNearOrOver) SpendlyCoral else SpendlyEmerald
            )
            Text(
              text = "$${remaining.toInt()} remaining",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isNearOrOver) SpendlyCoral else SpendlyOnSurfaceVariant
            )
          }
        }
      }
    }
  }
}
