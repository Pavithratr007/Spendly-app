package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ExpenseEntity
import com.example.data.SpendlyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: SpendlyRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = SpendlyRepository(db.expenseDao())
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Spendly", appName)
  }

  @Test
  fun `insert and retrieve expense`() = runBlocking {
    val expense = ExpenseEntity(
      title = "Sweetgreen",
      category = "Food",
      amount = 16.40,
      isIncome = false,
      dateMillis = System.currentTimeMillis()
    )
    val id = repository.insertExpense(expense)
    val all = repository.allExpenses.first()
    assertEquals(1, all.size)
    assertEquals("Sweetgreen", all[0].title)
    assertEquals(16.40, all[0].amount, 0.001)
  }

  @Test
  fun `seed initial data populates expenses and budgets`() = runBlocking {
    repository.seedInitialDataIfEmpty()
    val all = repository.allExpenses.first()
    val budgets = repository.allBudgets.first()
    val settings = repository.budgetSettings.first()

    assert(all.isNotEmpty())
    assert(budgets.isNotEmpty())
    assertNotNull(settings)
    assertEquals(4000.0, settings?.monthlyCap ?: 0.0, 0.01)
  }
}
