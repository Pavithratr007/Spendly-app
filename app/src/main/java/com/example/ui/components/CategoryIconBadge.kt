package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.model.SpendlyCategory

@Composable
fun CategoryIconBadge(
  category: SpendlyCategory,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  iconSize: Dp = 22.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .background(category.bgColor, RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = category.icon,
      contentDescription = category.displayName,
      tint = category.color,
      modifier = Modifier.size(iconSize)
    )
  }
}
