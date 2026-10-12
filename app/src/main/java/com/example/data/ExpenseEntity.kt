package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val title: String,
  val category: String,
  val amount: Double,
  val isIncome: Boolean = false,
  val dateMillis: Long = System.currentTimeMillis(),
  val note: String = "",
  val paymentAccount: String = "Apple Pay Visa • 4829",
  val isRecurring: Boolean = false,
  val receiptUri: String? = null
)
