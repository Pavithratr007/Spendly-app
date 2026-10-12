package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.ExpenseEntity
import com.example.ui.model.DefaultPaymentAccounts
import com.example.ui.model.ExpenseCategories
import com.example.ui.model.SpendlyCategory
import com.example.ui.theme.SpendlyBorder
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddExpenseScreen(
  existingExpense: ExpenseEntity? = null,
  onSaveExpense: (
    title: String,
    category: String,
    amount: Double,
    isIncome: Boolean,
    dateMillis: Long,
    note: String,
    paymentAccount: String,
    isRecurring: Boolean
  ) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  var amountText by remember {
    mutableStateOf(
      existingExpense?.let { String.format(Locale.US, "%.2f", it.amount) } ?: "42.50"
    )
  }
  var selectedCategory by remember {
    mutableStateOf(
      existingExpense?.let { SpendlyCategory.fromId(it.category) } ?: SpendlyCategory.FOOD
    )
  }
  var isIncome by remember { mutableStateOf(existingExpense?.isIncome ?: false) }
  var noteText by remember {
    mutableStateOf(existingExpense?.note ?: existingExpense?.title ?: "Dinner with Alex at Trattoria")
  }
  var selectedDateMillis by remember {
    mutableLongStateOf(existingExpense?.dateMillis ?: System.currentTimeMillis())
  }
  var selectedAccount by remember {
    mutableStateOf(existingExpense?.paymentAccount ?: "Apple Pay Visa • 4829")
  }
  var isRecurring by remember { mutableStateOf(existingExpense?.isRecurring ?: false) }
  var hasAttachedReceipt by remember { mutableStateOf(true) }
  var isEditingAmountInField by remember { mutableStateOf(false) }

  val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.US) }
  val currentAmountValue = amountText.toDoubleOrNull() ?: 0.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Amount Header & Display Card
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
            .padding(vertical = 20.dp, horizontal = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Pill: EXPENSE AMOUNT or INCOME
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFFEEF2FF))
              .clickable { isIncome = !isIncome }
              .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isIncome) SpendlyEmerald else SpendlyPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isIncome) "INCOME AMOUNT" else "EXPENSE AMOUNT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = if (isIncome) SpendlyEmerald else SpendlyPrimary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big Amount Input / Display
          if (isEditingAmountInField) {
            OutlinedTextField(
              value = amountText,
              onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                  amountText = input
                }
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyPrimary
              ),
              prefix = {
                Text(
                  "$",
                  fontSize = 32.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyPrimary
                )
              },
              trailingIcon = {
                IconButton(onClick = { isEditingAmountInField = false }) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = "Done")
                }
              },
              modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("amount_numeric_input")
            )
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable { isEditingAmountInField = true }
                .testTag("amount_display_clickable")
            ) {
              Text(
                text = "$",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyPrimary
              )
              Text(
                text = amountText.ifBlank { "0.00" },
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = SpendlyPrimary,
                letterSpacing = (-0.03).sp
              )
              // Blinking cursor visual indicator
              Box(
                modifier = Modifier
                  .padding(start = 4.dp)
                  .width(3.dp)
                  .height(38.dp)
                  .background(SpendlyPrimary)
              )
            }
            Text(
              text = "Tap to edit amount",
              fontSize = 12.sp,
              color = SpendlyOnSurfaceVariant,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Quick Amount Adjustment Chips: +$5, +$10, +$20, +$50, reset
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf(5, 10, 20, 50).forEach { plus ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFF1F5F9))
                  .clickable {
                    val current = amountText.toDoubleOrNull() ?: 0.0
                    amountText = String.format(Locale.US, "%.2f", current + plus)
                  }
                  .padding(horizontal = 12.dp, vertical = 7.dp)
                  .testTag("quick_add_$plus"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "+$$plus",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyPrimary
                )
              }
            }

            // Reset button
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .clickable { amountText = "0.00" }
                .testTag("reset_amount_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Amount",
                tint = SpendlyOnSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    // 2. Category Selection Grid (8 categories)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Select Category",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = SpendlyOnSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Required",
              fontSize = 11.sp,
              color = Color(0xFF3B82F6),
              fontWeight = FontWeight.Medium
            )
          }

          Text(
            text = selectedCategory.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpendlyPrimary
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2 rows of 4 columns
        val chunkedCategories = ExpenseCategories.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          chunkedCategories.forEach { rowCategories ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowCategories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(78.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) SpendlyPrimary else Color.White)
                    .border(
                      width = if (isSelected) 0.dp else 1.dp,
                      color = if (isSelected) Color.Transparent else SpendlyBorder,
                      shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { selectedCategory = cat }
                    .testTag("category_${cat.id.lowercase()}"),
                  contentAlignment = Alignment.Center
                ) {
                  // Checkmark indicator if selected
                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SpendlyPrimary,
                        modifier = Modifier.size(11.dp)
                      )
                    }
                  }

                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = cat.displayName,
                      tint = if (isSelected) Color.White else cat.color,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = cat.id,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.White else SpendlyOnSurface
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 3. Transaction Details Container (Date, Note, Payment, Recurring, Receipt)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Date selection
          Column {
            Text(
              text = "TRANSACTION DATE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = SpendlyOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Date display container
              Row(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFEEF2FF))
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = SpendlyPrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = dateFormat.format(Date(selectedDateMillis)),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = SpendlyOnSurface
                )
              }

              // Quick selector: Today
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFF1F5F9))
                  .clickable { selectedDateMillis = System.currentTimeMillis() }
                  .padding(horizontal = 10.dp, vertical = 10.dp)
                  .testTag("date_today_button"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Today",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyPrimary
                )
              }

              // Quick selector: Y'day
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFF1F5F9))
                  .clickable {
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    selectedDateMillis = cal.timeInMillis
                  }
                  .padding(horizontal = 10.dp, vertical = 10.dp)
                  .testTag("date_yesterday_button"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Y'day",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = SpendlyOnSurfaceVariant
                )
              }
            }
          }

          // Note & Description
          Column {
            Text(
              text = "NOTE & DESCRIPTION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = SpendlyOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = noteText,
              onValueChange = { noteText = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("note_input_field"),
              placeholder = {
                Text("What was this expense for?", fontSize = 13.sp, color = SpendlyOnSurfaceVariant)
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = SpendlyPrimary
                )
              },
              trailingIcon = {
                if (noteText.isNotEmpty()) {
                  IconButton(onClick = { noteText = "" }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                  }
                }
              },
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = SpendlyPrimary,
                unfocusedBorderColor = SpendlyBorder
              ),
              singleLine = true
            )
          }

          // Payment Account
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "PAYMENT ACCOUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = SpendlyOnSurfaceVariant
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = SpendlyEmerald,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "Encrypted",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = SpendlyEmerald
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(DefaultPaymentAccounts) { account ->
                val fullLabel = "${account.name} ${account.details}"
                val isSelected = selectedAccount.contains(account.name, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFFEEF2FF) else Color(0xFFF8FAFC))
                    .border(
                      width = 1.dp,
                      color = if (isSelected) SpendlyPrimary else SpendlyBorder,
                      shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedAccount = fullLabel }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .testTag("account_chip_${account.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CreditCard,
                      contentDescription = null,
                      tint = if (isSelected) SpendlyPrimary else SpendlyOnSurfaceVariant,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = fullLabel,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) SpendlyPrimary else SpendlyOnSurface
                    )
                  }
                }
              }
            }
          }

          // Recurring Expense Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Autorenew,
                  contentDescription = null,
                  tint = SpendlyPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Recurring expense",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyOnSurface
                )
                Text(
                  text = "Repeat automatically monthly",
                  fontSize = 11.sp,
                  color = SpendlyOnSurfaceVariant
                )
              }
            }

            Switch(
              checked = isRecurring,
              onCheckedChange = { isRecurring = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SpendlyPrimary
              ),
              modifier = Modifier.testTag("recurring_expense_switch")
            )
          }

          // Receipt & Evidence Attachment
          Column {
            Text(
              text = "RECEIPT & EVIDENCE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = SpendlyOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = { hasAttachedReceipt = !hasAttachedReceipt },
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("attach_receipt_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF))
              ) {
                Icon(
                  imageVector = Icons.Default.PhotoCamera,
                  contentDescription = null,
                  tint = SpendlyPrimary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (hasAttachedReceipt) "Receipt Attached" else "Attach Receipt",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = SpendlyPrimary
                )
              }

              if (hasAttachedReceipt) {
                // Receipt preview thumbnail card
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0))
                    .border(1.dp, SpendlyBorder, RoundedCornerShape(8.dp)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Receipt attached",
                    tint = SpendlyPrimary,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 4. Primary Save Expense Button
    item {
      Button(
        onClick = {
          if (currentAmountValue > 0) {
            onSaveExpense(
              noteText.ifBlank { selectedCategory.displayName },
              selectedCategory.id,
              currentAmountValue,
              isIncome,
              selectedDateMillis,
              noteText,
              selectedAccount,
              isRecurring
            )
          }
        },
        enabled = currentAmountValue > 0,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_expense_primary_button"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C49))
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (existingExpense != null) "Update Expense ($${String.format(Locale.US, "%.2f", currentAmountValue)})" else "Save Expense ($${String.format(Locale.US, "%.2f", currentAmountValue)})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }

    // 5. Secondary Action Buttons: Cancel and + Save & Add Next
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onCancel,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("cancel_expense_button"),
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
        ) {
          Text(
            text = "Cancel",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpendlyOnSurface
          )
        }

        Button(
          onClick = {
            if (currentAmountValue > 0) {
              onSaveExpense(
                noteText.ifBlank { selectedCategory.displayName },
                selectedCategory.id,
                currentAmountValue,
                isIncome,
                selectedDateMillis,
                noteText,
                selectedAccount,
                isRecurring
              )
              // Reset for next
              amountText = "0.00"
              noteText = ""
            }
          },
          enabled = currentAmountValue > 0,
          modifier = Modifier
            .weight(1.3f)
            .height(48.dp)
            .testTag("save_and_add_next_button"),
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF))
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SpendlyPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Save & Add Next",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SpendlyPrimary
          )
        }
      }
    }
  }
}
