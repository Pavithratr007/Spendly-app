package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendlyBorder
import com.example.ui.theme.SpendlyPrimary

enum class SpendlyNavTab(
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  HOME("Home", Icons.Filled.GridView, Icons.Outlined.GridView),
  EXPENSES("Expenses", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
  ANALYTICS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
  BUDGET("Budget", Icons.Filled.Savings, Icons.Outlined.Savings)
}

@Composable
fun SpendlyBottomBar(
  currentTab: SpendlyNavTab,
  onTabSelected: (SpendlyNavTab) -> Unit,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Color.White,
    shadowElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      HorizontalDivider(color = SpendlyBorder, thickness = 1.dp)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(64.dp)
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab 1: Home
        BottomNavItem(
          tab = SpendlyNavTab.HOME,
          isSelected = currentTab == SpendlyNavTab.HOME,
          onClick = { onTabSelected(SpendlyNavTab.HOME) }
        )

        // Tab 2: Expenses
        BottomNavItem(
          tab = SpendlyNavTab.EXPENSES,
          isSelected = currentTab == SpendlyNavTab.EXPENSES,
          onClick = { onTabSelected(SpendlyNavTab.EXPENSES) }
        )

        // Tab 3: Center Add Button (+)
        Box(
          modifier = Modifier
            .offset(y = (-6).dp)
            .size(54.dp)
            .shadow(
              elevation = 10.dp,
              shape = CircleShape,
              ambientColor = SpendlyPrimary,
              spotColor = SpendlyPrimary
            )
            .background(
              Brush.linearGradient(
                listOf(Color(0xFF4F46E5), Color(0xFF3525CD))
              ),
              shape = CircleShape
            )
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onAddClick
            )
            .testTag("bottom_nav_add_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Expense",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        // Tab 4: Analytics
        BottomNavItem(
          tab = SpendlyNavTab.ANALYTICS,
          isSelected = currentTab == SpendlyNavTab.ANALYTICS,
          onClick = { onTabSelected(SpendlyNavTab.ANALYTICS) }
        )

        // Tab 5: Budget
        BottomNavItem(
          tab = SpendlyNavTab.BUDGET,
          isSelected = currentTab == SpendlyNavTab.BUDGET,
          onClick = { onTabSelected(SpendlyNavTab.BUDGET) }
        )
      }
    }
  }
}

@Composable
private fun BottomNavItem(
  tab: SpendlyNavTab,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon
  val contentColor = if (isSelected) SpendlyPrimary else Color(0xFF94A3B8)

  Column(
    modifier = Modifier
      .padding(vertical = 4.dp, horizontal = 12.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .testTag("bottom_nav_tab_${tab.name.lowercase()}"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = tab.label,
      tint = contentColor,
      modifier = Modifier.size(24.dp)
    )
    Text(
      text = tab.label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
      color = contentColor
    )
  }
}
