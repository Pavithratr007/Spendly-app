package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BudgetSettingsEntity
import com.example.data.CategoryBudgetEntity
import com.example.data.ExpenseEntity
import com.example.data.SpendlyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AnalyticsTimeframe(val label: String) {
  WEEK("Week"),
  MONTH("Month"),
  QUARTER("Quarter"),
  YEAR("Year")
}

data class CategoryStat(
  val categoryId: String,
  val totalSpent: Double,
  val count: Int,
  val percentage: Float,
  val trendVsPrevMonth: Float
)

data class MonthSpendPoint(
  val label: String,
  val amount: Double,
  val isCurrent: Boolean = false
)

class SpendlyViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: SpendlyRepository

  init {
    val db = AppDatabase.getDatabase(application)
    repository = SpendlyRepository(db.expenseDao())
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty()
    }
  }

  val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allBudgets: StateFlow<List<CategoryBudgetEntity>> = repository.allBudgets.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val budgetSettings: StateFlow<BudgetSettingsEntity> = repository.budgetSettings.combine(
    MutableStateFlow(BudgetSettingsEntity())
  ) { current, default ->
    current ?: default
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = BudgetSettingsEntity()
  )

  // Filters for Expenses Screen
  val searchQuery = MutableStateFlow("")
  val selectedCategoryFilter = MutableStateFlow<String?>("All")
  val isThisMonthOnlyFilter = MutableStateFlow(false)

  // Analytics timeframe
  val analyticsTimeframe = MutableStateFlow(AnalyticsTimeframe.MONTH)
  val analyticsMonthOffset = MutableStateFlow(0) // 0 = current month, -1 = last month, etc.

  // Week/Month toggle on dashboard
  val dashboardActivityView = MutableStateFlow("Week") // "Week" or "Month"

  // Quick message snackbar / feedback
  private val _feedbackMessage = MutableStateFlow<String?>(null)
  val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

  fun clearFeedbackMessage() {
    _feedbackMessage.value = null
  }

  fun showFeedback(msg: String) {
    _feedbackMessage.value = msg
  }

  // Filtered expenses for Expenses Screen
  val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
    allExpenses,
    searchQuery,
    selectedCategoryFilter,
    isThisMonthOnlyFilter
  ) { list, query, category, thisMonthOnly ->
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    list.filter { item ->
      val matchesQuery = if (query.isBlank()) true else {
        item.title.contains(query, ignoreCase = true) ||
          item.note.contains(query, ignoreCase = true) ||
          item.paymentAccount.contains(query, ignoreCase = true) ||
          item.amount.toString().contains(query)
      }

      val matchesCategory = if (category == null || category == "All") true else {
        if (category == "Income") item.isIncome else item.category.equals(category, ignoreCase = true)
      }

      val matchesMonth = if (!thisMonthOnly) true else {
        val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
        itemCal.get(Calendar.MONTH) == currentMonth && itemCal.get(Calendar.YEAR) == currentYear
      }

      matchesQuery && matchesCategory && matchesMonth
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun addExpense(
    title: String,
    category: String,
    amount: Double,
    isIncome: Boolean,
    dateMillis: Long,
    note: String,
    paymentAccount: String,
    isRecurring: Boolean
  ) {
    viewModelScope.launch {
      val entity = ExpenseEntity(
        title = title.ifBlank { if (isIncome) "Income" else category },
        category = category,
        amount = amount,
        isIncome = isIncome,
        dateMillis = dateMillis,
        note = note,
        paymentAccount = paymentAccount,
        isRecurring = isRecurring
      )
      repository.insertExpense(entity)
      _feedbackMessage.value = if (isIncome) "Income of $${String.format(Locale.US, "%.2f", amount)} added" else "Expense of $${String.format(Locale.US, "%.2f", amount)} added"
    }
  }

  fun updateExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.updateExpense(expense)
      _feedbackMessage.value = "Updated ${expense.title}"
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
      _feedbackMessage.value = "Deleted ${expense.title}"
    }
  }

  fun updateMonthlyCap(cap: Double) {
    viewModelScope.launch {
      val current = budgetSettings.value
      repository.updateBudgetSettings(monthlyCap = cap, totalBalance = current.totalBalance)
      _feedbackMessage.value = "Monthly budget cap set to $${String.format(Locale.US, "%.2f", cap)}"
    }
  }

  fun updateTotalBalance(balance: Double) {
    viewModelScope.launch {
      val current = budgetSettings.value
      repository.updateBudgetSettings(monthlyCap = current.monthlyCap, totalBalance = balance)
      _feedbackMessage.value = "Total balance updated to $${String.format(Locale.US, "%.2f", balance)}"
    }
  }

  fun updateCategoryLimit(category: String, limit: Double) {
    viewModelScope.launch {
      repository.updateCategoryBudget(category, limit)
      _feedbackMessage.value = "$category budget updated to $${String.format(Locale.US, "%.2f", limit)}"
    }
  }

  fun exportExpensesAsCsv(): String {
    val expenses = allExpenses.value
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val sb = StringBuilder()
    sb.append("ID,Date,Title,Category,Type,Amount,Payment Method,Recurring,Note\n")
    for (e in expenses) {
      val dateStr = sdf.format(Date(e.dateMillis))
      val typeStr = if (e.isIncome) "INCOME" else "EXPENSE"
      val cleanTitle = e.title.replace(",", " ")
      val cleanNote = e.note.replace(",", " ")
      val cleanAccount = e.paymentAccount.replace(",", " ")
      sb.append("${e.id},\"$dateStr\",\"$cleanTitle\",\"${e.category}\",\"$typeStr\",${e.amount},\"$cleanAccount\",${e.isRecurring},\"$cleanNote\"\n")
    }
    return sb.toString()
  }
}
