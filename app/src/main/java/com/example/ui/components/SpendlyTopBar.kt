package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpendlyEmerald
import com.example.ui.theme.SpendlyOnSurface
import com.example.ui.theme.SpendlyOnSurfaceVariant
import com.example.ui.theme.SpendlyPrimary

@Composable
fun SpendlyTopBar(
  subtitle: String,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onProfileClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showBackButton) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = SpendlyOnSurface
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      // Spendly stylized logo
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF4F46E5), Color(0xFF3525CD))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AccountBalanceWallet,
          contentDescription = "Spendly Logo",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column {
        Text(
          text = "Spendly",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = SpendlyPrimary,
          letterSpacing = (-0.02).sp
        )
        Text(
          text = subtitle,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = SpendlyOnSurfaceVariant
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Notification bell
      IconButton(
        onClick = onNotificationClick,
        modifier = Modifier
          .size(40.dp)
          .testTag("notification_button")
      ) {
        BadgedBox(
          badge = {
            Badge(
              containerColor = SpendlyEmerald,
              modifier = Modifier.size(7.dp)
            )
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = SpendlyOnSurfaceVariant,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // User profile avatar (Alex)
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE))
            )
          )
          .clickable { onProfileClick() }
          .testTag("profile_avatar_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User profile",
          tint = SpendlyPrimary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
