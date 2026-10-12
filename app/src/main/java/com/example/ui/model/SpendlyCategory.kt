package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.BillsCategoryBg
import com.example.ui.theme.BillsCategoryColor
import com.example.ui.theme.FoodCategoryBg
import com.example.ui.theme.FoodCategoryColor
import com.example.ui.theme.FunCategoryBg
import com.example.ui.theme.FunCategoryColor
import com.example.ui.theme.GroceryCategoryBg
import com.example.ui.theme.GroceryCategoryColor
import com.example.ui.theme.HealthCategoryBg
import com.example.ui.theme.HealthCategoryColor
import com.example.ui.theme.OtherCategoryBg
import com.example.ui.theme.OtherCategoryColor
import com.example.ui.theme.ShoppingCategoryBg
import com.example.ui.theme.ShoppingCategoryColor
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyEmeraldLight
import com.example.ui.theme.TransitCategoryBg
import com.example.ui.theme.TransitCategoryColor

enum class SpendlyCategory(
  val id: String,
  val displayName: String,
  val icon: ImageVector,
  val color: Color,
  val bgColor: Color
) {
  FOOD(
    id = "Food",
    displayName = "Food & Dining",
    icon = Icons.Default.Restaurant,
    color = FoodCategoryColor,
    bgColor = FoodCategoryBg
  ),
  GROCERY(
    id = "Grocery",
    displayName = "Groceries",
    icon = Icons.Default.ShoppingCart,
    color = GroceryCategoryColor,
    bgColor = GroceryCategoryBg
  ),
  SHOPPING(
    id = "Shopping",
    displayName = "Shopping",
    icon = Icons.Default.ShoppingBag,
    color = ShoppingCategoryColor,
    bgColor = ShoppingCategoryBg
  ),
  TRANSIT(
    id = "Transit",
    displayName = "Transportation",
    icon = Icons.Default.DirectionsCar,
    color = TransitCategoryColor,
    bgColor = TransitCategoryBg
  ),
  FUN(
    id = "Fun",
    displayName = "Entertainment",
    icon = Icons.Default.SportsEsports,
    color = FunCategoryColor,
    bgColor = FunCategoryBg
  ),
  BILLS(
    id = "Bills",
    displayName = "Housing & Utilities",
    icon = Icons.Default.Receipt,
    color = BillsCategoryColor,
    bgColor = BillsCategoryBg
  ),
  HEALTH(
    id = "Health",
    displayName = "Health & Wellness",
    icon = Icons.Default.Favorite,
    color = HealthCategoryColor,
    bgColor = HealthCategoryBg
  ),
  OTHER(
    id = "Other",
    displayName = "Others",
    icon = Icons.Default.Category,
    color = OtherCategoryColor,
    bgColor = OtherCategoryBg
  ),
  INCOME(
    id = "Income",
    displayName = "Income",
    icon = Icons.AutoMirrored.Filled.TrendingUp,
    color = SpendlyEmerald,
    bgColor = SpendlyEmeraldLight
  );

  companion object {
    fun fromId(id: String): SpendlyCategory {
      return entries.firstOrNull { 
        it.id.equals(id, ignoreCase = true) || 
        it.displayName.equals(id, ignoreCase = true) 
      } ?: OTHER
    }
  }
}

val ExpenseCategories = listOf(
  SpendlyCategory.FOOD,
  SpendlyCategory.GROCERY,
  SpendlyCategory.SHOPPING,
  SpendlyCategory.TRANSIT,
  SpendlyCategory.FUN,
  SpendlyCategory.BILLS,
  SpendlyCategory.HEALTH,
  SpendlyCategory.OTHER
)

data class PaymentAccount(
  val id: String,
  val name: String,
  val details: String,
  val iconName: String = "card"
)

val DefaultPaymentAccounts = listOf(
  PaymentAccount("apple_pay", "Apple Pay", "Visa • 4829"),
  PaymentAccount("amex_gold", "Amex Gold", "• 1004"),
  PaymentAccount("checking", "Checking", "• 9102"),
  PaymentAccount("cash", "Cash", "Wallet")
)
