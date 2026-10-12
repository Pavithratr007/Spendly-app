package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SpendlyRepository(private val expenseDao: ExpenseDao) {

  val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
  val allBudgets: Flow<List<CategoryBudgetEntity>> = expenseDao.getAllBudgets()
  val budgetSettings: Flow<BudgetSettingsEntity?> = expenseDao.getBudgetSettings()

  suspend fun insertExpense(expense: ExpenseEntity): Long {
    return expenseDao.insertExpense(expense)
  }

  suspend fun updateExpense(expense: ExpenseEntity) {
    expenseDao.updateExpense(expense)
  }

  suspend fun deleteExpense(expense: ExpenseEntity) {
    expenseDao.deleteExpense(expense)
  }

  suspend fun deleteExpenseById(id: Long) {
    expenseDao.deleteExpenseById(id)
  }

  suspend fun updateCategoryBudget(category: String, limit: Double) {
    expenseDao.insertOrUpdateBudget(CategoryBudgetEntity(category, limit))
  }

  suspend fun updateBudgetSettings(monthlyCap: Double, totalBalance: Double) {
    expenseDao.saveBudgetSettings(BudgetSettingsEntity(id = 1, monthlyCap = monthlyCap, totalBalance = totalBalance))
  }

  suspend fun seedInitialDataIfEmpty() {
    val count = expenseDao.getExpenseCount()
    if (count > 0) return

    val cal = Calendar.getInstance()
    val now = cal.timeInMillis

    // Construct timestamps for recent days
    fun daysAgo(days: Int, hour: Int = 12, minute: Int = 0): Long {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_YEAR, -days)
      c.set(Calendar.HOUR_OF_DAY, hour)
      c.set(Calendar.MINUTE, minute)
      return c.timeInMillis
    }

    val sampleExpenses = listOf(
      ExpenseEntity(
        title = "Blue Bottle Coffee",
        category = "Food",
        amount = 7.25,
        isIncome = false,
        dateMillis = daysAgo(0, 9, 41),
        note = "Morning espresso",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Sweetgreen",
        category = "Food",
        amount = 16.40,
        isIncome = false,
        dateMillis = daysAgo(0, 13, 15),
        note = "Harvest bowl lunch",
        paymentAccount = "Visa • 4829"
      ),
      ExpenseEntity(
        title = "Whole Foods Market",
        category = "Grocery",
        amount = 84.30,
        isIncome = false,
        dateMillis = daysAgo(1, 18, 20),
        note = "Weekly organic groceries",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Shell Oil",
        category = "Transit",
        amount = 45.00,
        isIncome = false,
        dateMillis = daysAgo(1, 15, 40),
        note = "Fuel fill-up",
        paymentAccount = "Amex Gold • 1004"
      ),
      ExpenseEntity(
        title = "Netflix Subscription",
        category = "Fun",
        amount = 15.99,
        isIncome = false,
        dateMillis = daysAgo(1, 8, 0),
        note = "Monthly premium streaming",
        paymentAccount = "Amex Gold • 1004",
        isRecurring = true
      ),
      ExpenseEntity(
        title = "Freelance Project",
        category = "Income",
        amount = 1200.00,
        isIncome = true,
        dateMillis = daysAgo(3, 10, 30),
        note = "Design milestone payment",
        paymentAccount = "Checking • 0911"
      ),
      ExpenseEntity(
        title = "Target Superstore",
        category = "Shopping",
        amount = 112.50,
        isIncome = false,
        dateMillis = daysAgo(3, 16, 10),
        note = "Household items & supplies",
        paymentAccount = "Debit • 3312"
      ),
      ExpenseEntity(
        title = "AMC Theatres",
        category = "Fun",
        amount = 32.00,
        isIncome = false,
        dateMillis = daysAgo(3, 19, 45),
        note = "Movie tickets & popcorn",
        paymentAccount = "Visa • 4829"
      ),
      ExpenseEntity(
        title = "Monthly Salary",
        category = "Income",
        amount = 3850.00,
        isIncome = true,
        dateMillis = daysAgo(10, 9, 0),
        note = "Corporate payroll deposit",
        paymentAccount = "Checking • 9102"
      ),
      ExpenseEntity(
        title = "Whole Foods Market",
        category = "Grocery",
        amount = 64.20,
        isIncome = false,
        dateMillis = daysAgo(4, 14, 30),
        note = "Produce & snacks",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Uber Ride",
        category = "Transit",
        amount = 24.50,
        isIncome = false,
        dateMillis = daysAgo(5, 20, 15),
        note = "Downtown trip",
        paymentAccount = "Amex Gold • 1004"
      ),
      ExpenseEntity(
        title = "Starbucks Coffee",
        category = "Food",
        amount = 6.75,
        isIncome = false,
        dateMillis = daysAgo(5, 9, 0),
        note = "Iced latte",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Housing & Utilities",
        category = "Bills",
        amount = 468.10,
        isIncome = false,
        dateMillis = daysAgo(12, 11, 0),
        note = "Electricity, Water & Internet",
        paymentAccount = "Checking • 9102",
        isRecurring = true
      ),
      ExpenseEntity(
        title = "Dinner at Trattoria",
        category = "Food",
        amount = 42.50,
        isIncome = false,
        dateMillis = daysAgo(6, 19, 30),
        note = "Dinner with Alex at Trattoria",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Trader Joe's",
        category = "Grocery",
        amount = 95.40,
        isIncome = false,
        dateMillis = daysAgo(8, 17, 10),
        note = "Pantry restocking",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "PlayStation Plus",
        category = "Fun",
        amount = 79.99,
        isIncome = false,
        dateMillis = daysAgo(14, 12, 0),
        note = "Gaming subscription",
        paymentAccount = "Visa • 4829"
      ),
      ExpenseEntity(
        title = "Gym & Health Club",
        category = "Health",
        amount = 85.00,
        isIncome = false,
        dateMillis = daysAgo(15, 8, 30),
        note = "Monthly fitness membership",
        paymentAccount = "Checking • 9102",
        isRecurring = true
      ),
      ExpenseEntity(
        title = "Zara Apparel",
        category = "Shopping",
        amount = 78.40,
        isIncome = false,
        dateMillis = daysAgo(9, 15, 20),
        note = "Autumn jacket",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Pharmacy & Vitamins",
        category = "Health",
        amount = 42.10,
        isIncome = false,
        dateMillis = daysAgo(11, 14, 15),
        note = "Daily vitamins & skincare",
        paymentAccount = "Visa • 4829"
      ),
      ExpenseEntity(
        title = "Subway Pass Recharge",
        category = "Transit",
        amount = 60.00,
        isIncome = false,
        dateMillis = daysAgo(16, 8, 45),
        note = "Monthly metro transit card",
        paymentAccount = "Apple Pay Visa • 4829"
      ),
      ExpenseEntity(
        title = "Cloud Storage & Software",
        category = "Other",
        amount = 29.99,
        isIncome = false,
        dateMillis = daysAgo(18, 10, 0),
        note = "Backup storage",
        paymentAccount = "Amex Gold • 1004",
        isRecurring = true
      )
    )

    expenseDao.insertAllExpenses(sampleExpenses)

    val defaultBudgets = listOf(
      CategoryBudgetEntity("Food", 800.0),
      CategoryBudgetEntity("Grocery", 500.0),
      CategoryBudgetEntity("Shopping", 400.0),
      CategoryBudgetEntity("Transit", 300.0),
      CategoryBudgetEntity("Fun", 350.0),
      CategoryBudgetEntity("Bills", 500.0),
      CategoryBudgetEntity("Health", 200.0),
      CategoryBudgetEntity("Other", 150.0)
    )
    expenseDao.insertAllBudgets(defaultBudgets)

    expenseDao.saveBudgetSettings(
      BudgetSettingsEntity(
        id = 1,
        monthlyCap = 4000.0,
        totalBalance = 8450.20
      )
    )
  }
}
