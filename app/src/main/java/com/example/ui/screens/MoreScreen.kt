package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import com.example.R
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkGreyCard
import com.example.ui.theme.DarkGreySurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.util.VibrationHelper

@Composable
fun MoreScreen(
    onClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("more_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Emblem Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkGreyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_custom),
                        contentDescription = "PinterestDL",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "PinterestDL",
                    color = TextWhite,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "v1.2.0 • Pure Black AMOLED",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Settings Section Header
        Text(
            text = "STORAGE & PREFERENCES",
            color = TextSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingRow(
            icon = Icons.Default.Folder,
            title = "Download directory",
            subtitle = "Pictures/PinterestDL",
            onClick = {
                VibrationHelper.vibrateClick(context, 20L)
                Toast.makeText(context, "Saved to Pictures/PinterestDL", Toast.LENGTH_SHORT).show()
            }
        )

        SettingRow(
            icon = Icons.Default.HighQuality,
            title = "Image quality",
            subtitle = "Highest resolution available (Original)",
            onClick = {
                VibrationHelper.vibrateClick(context, 20L)
                Toast.makeText(context, "All pins download in original resolution", Toast.LENGTH_SHORT).show()
            }
        )

        SettingRow(
            icon = Icons.Default.Vibration,
            title = "Haptic feedback",
            subtitle = "Tactile click vibrations enabled",
            onClick = {
                VibrationHelper.vibrateClick(context, 35L)
                Toast.makeText(context, "Haptic feedback active", Toast.LENGTH_SHORT).show()
            }
        )

        SettingRow(
            icon = Icons.Default.CleaningServices,
            title = "Clear image cache",
            subtitle = "Free up device RAM and thumbnail cache",
            onClick = {
                VibrationHelper.vibrateClick(context, 25L)
                context.imageLoader.diskCache?.clear()
                context.imageLoader.memoryCache?.clear()
                Toast.makeText(context, "Image cache cleared!", Toast.LENGTH_SHORT).show()
            }
        )

        SettingRow(
            icon = Icons.Default.DeleteSweep,
            title = "Clear download history",
            subtitle = "Clear database list without deleting saved files",
            onClick = {
                VibrationHelper.vibrateClick(context, 20L)
                showClearHistoryDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Developer Credit & Contact
        Text(
            text = "DEVELOPER & ABOUT",
            color = TextSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Developer Profile Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkGreySurface)
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Developer Email", "Sasuke.winx@gmail.com"))
                    VibrationHelper.vibrateClick(context, 25L)
                    Toast.makeText(context, "Copied Sasuke.winx@gmail.com to clipboard!", Toast.LENGTH_SHORT).show()
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Developer: ",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "ѕαѕυкє",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Sasuke.winx@gmail.com",
                        color = TextSecondary,
                        fontSize = 12.5.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Email",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Email Direct Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkGreySurface)
                .clickable {
                    VibrationHelper.vibrateClick(context, 20L)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:Sasuke.winx@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "PinterestDL Feedback")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Contact Sasuke"))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Email: Sasuke.winx@gmail.com", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Send Developer Feedback",
                    color = TextWhite,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Email",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Description
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111114))
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "PinterestDL is a high-speed media downloader built with AMOLED pure black theme and multi-threaded parallel downloads.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Clear History Confirm Dialog
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                containerColor = DarkGreySurface,
                title = {
                    Text("Clear Download History?", color = TextWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "This will clear the list of downloaded items from the app without removing the files from your storage.",
                        color = TextSecondary,
                        fontSize = 13.5.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearHistoryDialog = false
                            onClearHistory()
                            Toast.makeText(context, "Download history cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 11.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
