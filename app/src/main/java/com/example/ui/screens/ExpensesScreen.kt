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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.model.ExpenseCategories
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyBorder
import com.example.ui.theme.SpendlyDeepEmerald
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import com.example.ui.viewmodel.SpendlyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
  viewModel: SpendlyViewModel,
  onExpenseClick: (ExpenseEntity) -> Unit,
  onExportCsvClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allExpenses by viewModel.allExpenses.collectAsState()
  val filteredExpenses by viewModel.filteredExpenses.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
  val isThisMonthOnly by viewModel.isThisMonthOnlyFilter.collectAsState()

  // Calculate this month summary
  val cal = Calendar.getInstance()
  val currentMonth = cal.get(Calendar.MONTH)
  val currentYear = cal.get(Calendar.YEAR)
  val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)

  val thisMonthExpenses = allExpenses.filter {
    val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
  }
  val thisMonthTotal = thisMonthExpenses.filter { !it.isIncome }.sumOf { it.amount }
  val thisMonthCount = thisMonthExpenses.size

  // Group filtered expenses by date header
  val groupedExpenses = remember(filteredExpenses) {
    val dateHeaderFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayHeaderFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
    val todayStr = dateHeaderFormat.format(Date())

    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayStr = dateHeaderFormat.format(yesterdayCal.time)

    filteredExpenses.groupBy { dateHeaderFormat.format(Date(it.dateMillis)) }
      .map { (dateKey, items) ->
        val headerTitle = when (dateKey) {
          todayStr -> "TODAY, ${SimpleDateFormat("MMM d", Locale.US).format(Date()).uppercase()}"
          yesterdayStr -> "YESTERDAY, ${SimpleDateFormat("MMM d", Locale.US).format(yesterdayCal.time).uppercase()}"
          else -> displayHeaderFormat.format(Date(items.first().dateMillis)).uppercase()
        }
        val netSum = items.sumOf { if (it.isIncome) it.amount else -it.amount }
        Triple(headerTitle, netSum, items)
      }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // 1. Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { viewModel.searchQuery.value = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp, bottom = 10.dp)
        .testTag("expenses_search_input"),
      placeholder = {
        Text("Search merchant, note, or amount...", fontSize = 13.sp, color = SpendlyOnSurfaceVariant)
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = SpendlyOnSurfaceVariant
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { viewModel.searchQuery.value = "" }) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = SpendlyPrimary,
        unfocusedBorderColor = SpendlyBorder
      ),
      singleLine = true
    )

    // 2. Horizontal Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 12.dp)
    ) {
      // Filter Icon button
      item {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selectedCategory != "All" || isThisMonthOnly) SpendlyPrimary else Color(0xFFF1F5F9))
            .clickable {
              viewModel.selectedCategoryFilter.value = "All"
              viewModel.isThisMonthOnlyFilter.value = false
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filters",
            tint = if (selectedCategory != "All" || isThisMonthOnly) Color.White else SpendlyOnSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // All chip
      item {
        FilterPill(
          label = "All (${allExpenses.size})",
          isSelected = selectedCategory == "All" && !isThisMonthOnly,
          onClick = {
            viewModel.selectedCategoryFilter.value = "All"
            viewModel.isThisMonthOnlyFilter.value = false
          }
        )
      }

      // This Month chip
      item {
        FilterPill(
          label = "This Month",
          isSelected = isThisMonthOnly,
          onClick = {
            viewModel.isThisMonthOnlyFilter.value = !isThisMonthOnly
          }
        )
      }

      // Category chips
      items(ExpenseCategories) { cat ->
        FilterPill(
          label = cat.displayName,
          isSelected = selectedCategory == cat.id,
          onClick = {
            viewModel.selectedCategoryFilter.value = if (selectedCategory == cat.id) "All" else cat.id
          }
        )
      }

      // Income chip
      item {
        FilterPill(
          label = "Income",
          isSelected = selectedCategory == "Income",
          onClick = {
            viewModel.selectedCategoryFilter.value = if (selectedCategory == "Income") "All" else "Income"
          }
        )
      }
    }

    // 3. Month Total Banner
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = SpendlyPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "$monthName Total",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = SpendlyOnSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "$${String.format(Locale.US, "%,.2f", thisMonthTotal)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyOnSurface
              )
              Text(
                text = " ($thisMonthCount txns)",
                fontSize = 12.sp,
                color = SpendlyOnSurfaceVariant,
                modifier = Modifier.padding(bottom = 1.dp)
              )
            }
          }
        }

        // CSV Export button
        Button(
          onClick = onExportCsvClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("csv_export_button")
        ) {
          Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            tint = SpendlyPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "CSV",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyPrimary
          )
        }
      }
    }

    // 4. Grouped Transactions List
    if (filteredExpenses.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "No transactions found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try adjusting your search query or active filters",
            fontSize = 13.sp,
            color = SpendlyOnSurfaceVariant
          )
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = {
              viewModel.searchQuery.value = ""
              viewModel.selectedCategoryFilter.value = "All"
              viewModel.isThisMonthOnlyFilter.value = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary)
          ) {
            Text("Reset Filters")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        groupedExpenses.forEach { (dateHeader, netSum, items) ->
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = dateHeader,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = SpendlyOnSurfaceVariant
              )
              Text(
                text = if (netSum >= 0) "+$${String.format(Locale.US, "%,.2f", netSum)} Net" else "-$${String.format(Locale.US, "%,.2f", -netSum)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (netSum >= 0) SpendlyDeepEmerald else SpendlyOnSurfaceVariant
              )
            }
          }

          items(items) { expense ->
            ExpenseFeedItemCard(
              expense = expense,
              onClick = { onExpenseClick(expense) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExpenseFeedItemCard(
  expense: ExpenseEntity,
  onClick: () -> Unit
) {
  val cat = if (expense.isIncome) SpendlyCategory.INCOME else SpendlyCategory.fromId(expense.category)
  val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }
  val formattedTime = timeFormat.format(Date(expense.dateMillis))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("expense_card_${expense.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        CategoryIconBadge(
          category = cat,
          size = 46.dp,
          iconSize = 22.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = expense.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface,
            maxLines = 1
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (expense.isIncome) "Income" else cat.displayName,
              fontSize = 12.sp,
              color = SpendlyOnSurfaceVariant
            )

            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFCBD5E1))
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Payment method pill badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = expense.paymentAccount,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SpendlyOnSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = formattedTime,
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (expense.isIncome) "+$${String.format(Locale.US, "%,.2f", expense.amount)}" else "-$${String.format(Locale.US, "%,.2f", expense.amount)}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (expense.isIncome) SpendlyDeepEmerald else SpendlyOnSurface
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = null,
          tint = Color(0xFFCBD5E1),
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

@Composable
private fun FilterPill(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) SpendlyPrimary else Color.White)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("filter_pill_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Color.White else SpendlyOnSurfaceVariant
    )
  }
}
