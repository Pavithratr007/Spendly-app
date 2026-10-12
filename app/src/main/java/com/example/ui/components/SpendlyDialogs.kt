package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryBudgetEntity
import com.example.data.ExpenseEntity
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyCoral
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseDetailDialog(
  expense: ExpenseEntity,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val cat = if (expense.isIncome) SpendlyCategory.INCOME else SpendlyCategory.fromId(expense.category)
  val sdf = remember { SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.US) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        CategoryIconBadge(category = cat, size = 44.dp, iconSize = 22.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = expense.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyOnSurface
          )
          Text(
            text = cat.displayName,
            fontSize = 12.sp,
            color = SpendlyOnSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Amount callout
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (expense.isIncome) "+$${String.format(Locale.US, "%,.2f", expense.amount)}" else "-$${String.format(Locale.US, "%,.2f", expense.amount)}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = if (expense.isIncome) SpendlyEmerald else SpendlyPrimary
          )
        }

        DetailLine("Date & Time", sdf.format(Date(expense.dateMillis)))
        DetailLine("Payment Method", expense.paymentAccount)
        if (expense.note.isNotBlank() && expense.note != expense.title) {
          DetailLine("Note", expense.note)
        }
        DetailLine("Recurring", if (expense.isRecurring) "Yes (Monthly)" else "No")
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onDismiss()
            onEdit()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("dialog_edit_expense_button")
        ) {
          Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit")
        }

        OutlinedButton(
          onClick = {
            onDismiss()
            onDelete()
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyCoral),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("dialog_delete_expense_button")
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Delete")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = SpendlyOnSurfaceVariant)
      }
    }
  )
}

@Composable
private fun DetailLine(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 12.sp, color = SpendlyOnSurfaceVariant)
    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SpendlyOnSurface)
  }
}

@Composable
fun SetMonthlyBudgetDialog(
  currentCap: Double,
  currentBalance: Double,
  onDismiss: () -> Unit,
  onSave: (newCap: Double, newBalance: Double) -> Unit
) {
  var capText by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentCap)) }
  var balanceText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentBalance)) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = Color.White,
    title = {
      Text(
        text = "Set Spending Limits & Balance",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = SpendlyOnSurface
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Configure your monthly spending cap and current total available balance.",
          fontSize = 13.sp,
          color = SpendlyOnSurfaceVariant
        )

        OutlinedTextField(
          value = capText,
          onValueChange = { capText = it },
          label = { Text("Monthly Spending Cap ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_monthly_cap")
        )

        OutlinedTextField(
          value = balanceText,
          onValueChange = { balanceText = it },
          label = { Text("Available Balance ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_available_balance")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cap = capText.toDoubleOrNull() ?: currentCap
          val bal = balanceText.toDoubleOrNull() ?: currentBalance
          onSave(cap, bal)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_budget_cap_button")
      ) {
        Text("Save Settings")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = SpendlyOnSurfaceVariant)
      }
    }
  )
}

@Composable
fun EditCategoryLimitDialog(
  budget: CategoryBudgetEntity,
  onDismiss: () -> Unit,
  onSave: (newLimit: Double) -> Unit
) {
  val cat = SpendlyCategory.fromId(budget.category)
  var limitText by remember { mutableStateOf(String.format(Locale.US, "%.0f", budget.monthlyLimit)) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryIconBadge(category = cat, size = 38.dp, iconSize = 18.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "${cat.displayName} Limit",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = SpendlyOnSurface
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Update your monthly allowance for ${cat.displayName}.",
          fontSize = 13.sp,
          color = SpendlyOnSurfaceVariant
        )

        OutlinedTextField(
          value = limitText,
          onValueChange = { limitText = it },
          label = { Text("Monthly Allowance ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_category_limit")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val limit = limitText.toDoubleOrNull() ?: budget.monthlyLimit
          onSave(limit)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_category_limit_button")
      ) {
        Text("Update Limit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = SpendlyOnSurfaceVariant)
      }
    }
  )
}

@Composable
fun TransferDialog(
  onDismiss: () -> Unit,
  onTransfer: (amount: Double, fromAccount: String, toAccount: String) -> Unit
) {
  var amountText by remember { mutableStateOf("250.00") }
  var fromAccount by remember { mutableStateOf("Checking • 9102") }
  var toAccount by remember { mutableStateOf("Savings Vault") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = Color.White,
    title = {
      Text(
        text = "Quick Transfer",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = SpendlyOnSurface
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Move funds between your accounts or savings vaults.",
          fontSize = 13.sp,
          color = SpendlyOnSurfaceVariant
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("input_transfer_amount")
        )

        OutlinedTextField(
          value = fromAccount,
          onValueChange = { fromAccount = it },
          label = { Text("From") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = toAccount,
          onValueChange = { toAccount = it },
          label = { Text("To") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 0.0
          if (amt > 0) onTransfer(amt, fromAccount, toAccount)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_transfer_button")
      ) {
        Text("Transfer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = SpendlyOnSurfaceVariant)
      }
    }
  )
}

@Composable
fun ReportPreviewDialog(
  title: String,
  reportText: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(22.dp),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SpendlyPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = SpendlyOnSurface
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = reportText,
          fontSize = 12.sp,
          color = SpendlyOnSurface,
          maxLines = 14
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = SpendlyPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Done")
      }
    }
  )
}
