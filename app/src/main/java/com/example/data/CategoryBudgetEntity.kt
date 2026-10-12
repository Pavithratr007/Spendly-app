package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
  @PrimaryKey
  val category: String,
  val monthlyLimit: Double
)

@Entity(tableName = "budget_settings")
data class BudgetSettingsEntity(
  @PrimaryKey
  val id: Int = 1,
  val monthlyCap: Double = 4000.0,
  val totalBalance: Double = 8450.20
)
