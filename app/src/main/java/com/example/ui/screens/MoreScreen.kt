package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkGreyCard
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

@Composable
fun MoreScreen(
    onClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var autoScanGallery by remember { mutableStateOf(true) }
    var autoRetryDownloads by remember { mutableStateOf(true) }
    var highResPreferred by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("more_screen")
    ) {
        // Top Header
        Text(
            text = "Settings",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. DEVELOPER CREDIT SECTION (Required)
        DeveloperCreditCard(context = context)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. DOWNLOAD & STORAGE SETTINGS (YTDLnis Style)
        SectionHeader(title = "Download & Storage", icon = Icons.Default.Download)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Download Path
                SettingInfoRow(
                    title = "Download directory",
                    subtitle = "Pictures/PinterestDL",
                    caption = "Automatically visible in Gallery & Google Photos",
                    icon = Icons.Default.Folder
                )

                Spacer(modifier = Modifier.height(14.dp))

                // File Naming Template
                SettingInfoRow(
                    title = "File naming scheme",
                    subtitle = "Sasuke-%(title)s.%(ext)s",
                    caption = "Spaces are automatically converted to underscores (e.g. Sasuke-Naruto_Cosplay.jpg)",
                    icon = Icons.Default.Tune,
                    highlightSubtitle = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quality Preference Switch
                SettingSwitchRow(
                    title = "Prefer highest resolution (Original / 736x)",
                    caption = "Downloads original resolution when available",
                    checked = highResPreferred,
                    onCheckedChange = { highResPreferred = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Auto Scan Switch
                SettingSwitchRow(
                    title = "Register in MediaStore Gallery",
                    caption = "Index newly downloaded pins into device photos immediately",
                    checked = autoScanGallery,
                    onCheckedChange = { autoScanGallery = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow(
                    title = "Auto-retry failed downloads",
                    caption = "Automatically retry on temporary network dropouts",
                    checked = autoRetryDownloads,
                    onCheckedChange = { autoRetryDownloads = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. APPEARANCE SETTINGS (YTDLnis Style)
        SectionHeader(title = "Appearance", icon = Icons.Default.Palette)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingInfoRow(
                    title = "Color theme",
                    subtitle = "AMOLED Pitch Black (#0F0F11)",
                    caption = "Optimized for OLED displays with maximum power efficiency",
                    icon = Icons.Default.Palette
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingInfoRow(
                    title = "Accent color",
                    subtitle = "Pinterest Crimson (#FF2E55)",
                    caption = "Matches official brand and YTDLnis highlights",
                    icon = Icons.Default.Tune,
                    highlightSubtitle = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingInfoRow(
                    title = "Home layout",
                    subtitle = "4-Column Compact Grid",
                    caption = "High-density image grid with instant download icons",
                    icon = Icons.Default.Tune
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. DATA & CACHE MANAGEMENT
        SectionHeader(title = "Data & Storage", icon = Icons.Default.CleaningServices)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Clear Image Cache
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            context.imageLoader.diskCache?.clear()
                            context.imageLoader.memoryCache?.clear()
                            Toast.makeText(context, "Image cache cleared!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear thumbnail cache",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Frees up temporary cache while keeping saved files",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clear Download History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onClearHistory()
                            Toast.makeText(context, "Download history cleared", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear download records",
                            color = PinterestRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Clears app history list without deleting local pictures",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = PinterestRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. ABOUT APP
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About PinterestDL",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PinterestDL is a high-speed Pinterest media browser and bulk downloader with pixel-perfect YTDLnis dark aesthetics and instant gallery synchronization.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version 1.0.0 • YTDLnis Edition",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Bottom nav spacer
        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
private fun DeveloperCreditCard(context: Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, PinterestRed.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("developer_credit_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PinterestRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PinterestRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Developer Credit",
                        color = PinterestRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lead Developer & System Architect",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stylized Watermark: ѕαѕυкє (glowing reddish-pink font)
            Surface(
                color = Color(0xFF131315),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28282D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ѕαѕυкє",
                        color = PinterestRed,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Email: Sasuke.winx@gmail.com
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF242428))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sasuke.winx@gmail.com",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Developer Email", "Sasuke.winx@gmail.com")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Email",
                        tint = PinterestRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:Sasuke.winx@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "PinterestDL Inquiries & Feedback")
                    }
                    context.startActivity(Intent.createChooser(intent, "Contact Sasuke"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinterestRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact Sasuke (Email)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PinterestRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingInfoRow(
    title: String,
    subtitle: String,
    caption: String,
    icon: ImageVector,
    highlightSubtitle: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = if (highlightSubtitle) PinterestRed else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = caption,
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    caption: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = caption,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PinterestRed,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0xFF2E2E32)
            )
        )
    }
}
