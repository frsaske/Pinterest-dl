package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkPillHighlight
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.VibrationHelper

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * Exact replica of YTDLnis bottom navigation bar.
 * Selected tab features the pill indicator exclusively enclosing the icon,
 * with high-contrast text label underneath.
 */
@Composable
fun YtdlnisBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    downloadsBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val items = listOf(
        NavItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavItem(
            title = "Downloads",
            selectedIcon = Icons.Filled.FileDownload,
            unselectedIcon = Icons.Outlined.FileDownload,
            badgeCount = downloadsBadgeCount
        ),
        NavItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack)
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTab == index

                val indicatorColor by animateColorAsState(
                    targetValue = if (isSelected) DarkPillHighlight else Color.Transparent,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "tab_pill_color"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) TextWhite else TextMuted,
                    animationSpec = tween(durationMillis = 180),
                    label = "tab_text_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                VibrationHelper.vibrateClick(context, 20L)
                                onTabSelected(index)
                            }
                        }
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${item.title.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pill indicator containing the icon
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(indicatorColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.badgeCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color.White,
                                        contentColor = PitchBlack,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Text(
                                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) TextWhite else TextMuted,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) TextWhite else TextMuted,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.title,
                        color = labelColor,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}
