package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
  fun getExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
  suspend fun getExpenseById(id: Long): ExpenseEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)

  @Update
  suspend fun updateExpense(expense: ExpenseEntity)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: Long)

  @Query("SELECT COUNT(*) FROM expenses")
  suspend fun getExpenseCount(): Int

  // Category Budgets
  @Query("SELECT * FROM category_budgets")
  fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateBudget(budget: CategoryBudgetEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllBudgets(budgets: List<CategoryBudgetEntity>)

  // Budget Settings
  @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
  fun getBudgetSettings(): Flow<BudgetSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveBudgetSettings(settings: BudgetSettingsEntity)
}
