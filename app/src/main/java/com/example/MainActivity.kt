package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.CategoryBudgetEntity
import com.example.data.ExpenseEntity
import com.example.ui.components.EditCategoryLimitDialog
import com.example.ui.components.ExpenseDetailDialog
import com.example.ui.components.ReportPreviewDialog
import com.example.ui.components.SetMonthlyBudgetDialog
import com.example.ui.components.SpendlyBottomBar
import com.example.ui.components.SpendlyNavTab
import com.example.ui.components.SpendlyTopBar
import com.example.ui.components.TransferDialog
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SpendlyViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

  private val viewModel: SpendlyViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SpendlyApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun SpendlyApp(viewModel: SpendlyViewModel) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Navigation State
  var currentTab by remember { mutableStateOf(SpendlyNavTab.HOME) }
  var isAddingExpense by remember { mutableStateOf(false) }
  var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

  // Dialog states
  var selectedExpenseForDetail by remember { mutableStateOf<ExpenseEntity?>(null) }
  var showSetBudgetDialog by remember { mutableStateOf(false) }
  var categoryBudgetToEdit by remember { mutableStateOf<CategoryBudgetEntity?>(null) }
  var showTransferDialog by remember { mutableStateOf(false) }
  var reportDialogTitle by remember { mutableStateOf<String?>(null) }
  var reportDialogContent by remember { mutableStateOf<String?>(null) }

  val feedbackMessage by viewModel.feedbackMessage.collectAsState()
  LaunchedEffect(feedbackMessage) {
    feedbackMessage?.let { msg ->
      scope.launch {
        snackbarHostState.showSnackbar(msg)
        viewModel.clearFeedbackMessage()
      }
    }
  }

  // Handle system back navigation
  BackHandler(enabled = isAddingExpense || expenseToEdit != null || currentTab != SpendlyNavTab.HOME) {
    if (isAddingExpense || expenseToEdit != null) {
      isAddingExpense = false
      expenseToEdit = null
    } else if (currentTab != SpendlyNavTab.HOME) {
      currentTab = SpendlyNavTab.HOME
    }
  }

  val budgetSettings by viewModel.budgetSettings.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      val subtitle = when {
        isAddingExpense -> "Add Expense"
        expenseToEdit != null -> "Edit Expense"
        currentTab == SpendlyNavTab.HOME -> "Dashboard"
        currentTab == SpendlyNavTab.EXPENSES -> "Expenses Feed"
        currentTab == SpendlyNavTab.ANALYTICS -> "Analytics"
        currentTab == SpendlyNavTab.BUDGET -> "Budget Goals"
        else -> "Expense Tracker"
      }

      SpendlyTopBar(
        subtitle = subtitle,
        showBackButton = isAddingExpense || expenseToEdit != null,
        onBackClick = {
          isAddingExpense = false
          expenseToEdit = null
        },
        onNotificationClick = {
          Toast.makeText(context, "Spendly Insight: You are 12% under budget this month!", Toast.LENGTH_SHORT).show()
        },
        onProfileClick = {
          Toast.makeText(context, "Logged in as Alex (Spendly Pro)", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.statusBarsPadding()
      )
    },
    bottomBar = {
      if (!isAddingExpense && expenseToEdit == null) {
        SpendlyBottomBar(
          currentTab = currentTab,
          onTabSelected = { tab ->
            currentTab = tab
          },
          onAddClick = {
            expenseToEdit = null
            isAddingExpense = true
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when {
        isAddingExpense || expenseToEdit != null -> {
          AddExpenseScreen(
            existingExpense = expenseToEdit,
            onSaveExpense = { title, category, amount, isIncome, dateMillis, note, paymentAccount, isRecurring ->
              if (expenseToEdit != null) {
                viewModel.updateExpense(
                  expenseToEdit!!.copy(
                    title = title,
                    category = category,
                    amount = amount,
                    isIncome = isIncome,
                    dateMillis = dateMillis,
                    note = note,
                    paymentAccount = paymentAccount,
                    isRecurring = isRecurring
                  )
                )
              } else {
                viewModel.addExpense(
                  title = title,
                  category = category,
                  amount = amount,
                  isIncome = isIncome,
                  dateMillis = dateMillis,
                  note = note,
                  paymentAccount = paymentAccount,
                  isRecurring = isRecurring
                )
              }
              isAddingExpense = false
              expenseToEdit = null
            },
            onCancel = {
              isAddingExpense = false
              expenseToEdit = null
            }
          )
        }

        currentTab == SpendlyNavTab.HOME -> {
          DashboardScreen(
            viewModel = viewModel,
            onNavigateToExpenses = { currentTab = SpendlyNavTab.EXPENSES },
            onNavigateToBudget = { currentTab = SpendlyNavTab.BUDGET },
            onAddExpenseClick = {
              expenseToEdit = null
              isAddingExpense = true
            },
            onExpenseClick = { expense ->
              selectedExpenseForDetail = expense
            },
            onOpenSetLimitDialog = {
              showSetBudgetDialog = true
            },
            onOpenTransferDialog = {
              showTransferDialog = true
            }
          )
        }

        currentTab == SpendlyNavTab.EXPENSES -> {
          ExpensesScreen(
            viewModel = viewModel,
            onExpenseClick = { expense ->
              selectedExpenseForDetail = expense
            },
            onExportCsvClick = {
              val csv = viewModel.exportExpensesAsCsv()
              reportDialogTitle = "Export CSV Data"
              reportDialogContent = "Data exported successfully:\n\n$csv"
            }
          )
        }

        currentTab == SpendlyNavTab.ANALYTICS -> {
          AnalyticsScreen(
            viewModel = viewModel,
            onDownloadReportClick = {
              reportDialogTitle = "Spendly Monthly Report"
              reportDialogContent = "Spendly Analytics Report:\n\nMonth: October 2024\nTotal Expenses: $2,340.50\nIncome: $5,050.00\nNet Savings: +$2,709.50\n\nTop Spending Categories:\n1. Food & Dining: $725.55 (31%)\n2. Groceries: $514.90 (22%)\n3. Housing & Utilities: $468.10 (20%)\n4. Entertainment: $280.86 (12%)\n\nBudget Health: Strong (12% lower than September)."
            },
            onOpenBudgetHealthDetails = {
              reportDialogTitle = "Budget Health Details"
              reportDialogContent = "Your Budget Health is rated 'Strong'!\n\n• Current spending is $159.50 below the $2,500 target.\n• Category pacing in Food & Dining decreased by 12.3% vs September.\n• Savings rate is on track for 38% of monthly income."
            }
          )
        }

        currentTab == SpendlyNavTab.BUDGET -> {
          BudgetScreen(
            viewModel = viewModel,
            onEditMonthlyCap = {
              showSetBudgetDialog = true
            },
            onEditCategoryBudget = { budget ->
              categoryBudgetToEdit = budget
            }
          )
        }
      }
    }
  }

  // Dialog: Transaction Detail & Actions
  selectedExpenseForDetail?.let { expense ->
    ExpenseDetailDialog(
      expense = expense,
      onDismiss = { selectedExpenseForDetail = null },
      onEdit = {
        expenseToEdit = expense
        isAddingExpense = false
      },
      onDelete = {
        viewModel.deleteExpense(expense)
      }
    )
  }

  // Dialog: Set Monthly Spending Cap & Available Balance
  if (showSetBudgetDialog) {
    SetMonthlyBudgetDialog(
      currentCap = budgetSettings.monthlyCap,
      currentBalance = budgetSettings.totalBalance,
      onDismiss = { showSetBudgetDialog = false },
      onSave = { newCap, newBalance ->
        viewModel.updateMonthlyCap(newCap)
        viewModel.updateTotalBalance(newBalance)
      }
    )
  }

  // Dialog: Edit Category Budget Limit
  categoryBudgetToEdit?.let { budget ->
    EditCategoryLimitDialog(
      budget = budget,
      onDismiss = { categoryBudgetToEdit = null },
      onSave = { newLimit ->
        viewModel.updateCategoryLimit(budget.category, newLimit)
      }
    )
  }

  // Dialog: Quick Transfer
  if (showTransferDialog) {
    TransferDialog(
      onDismiss = { showTransferDialog = false },
      onTransfer = { amount, from, to ->
        viewModel.showFeedback("Transferred $${String.format(Locale.US, "%.2f", amount)} from $from to $to")
      }
    )
  }

  // Dialog: Report / CSV Preview
  if (reportDialogTitle != null && reportDialogContent != null) {
    ReportPreviewDialog(
      title = reportDialogTitle!!,
      reportText = reportDialogContent!!,
      onDismiss = {
        reportDialogTitle = null
        reportDialogContent = null
      }
    )
  }
}
