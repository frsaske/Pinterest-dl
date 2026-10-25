package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGreySurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite

@Composable
fun PinterestTopBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onQuickFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Logo Row: "Pinterest" in #FF2E55 + "DL" in White
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_logo")
            ) {
                // PinterestDL Stylized Logo
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = PinterestRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("Pinterest")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        ) {
                            append("DL")
                        }
                    }
                )
            }

            // Stylized Developer Watermark in Top Right
            Text(
                text = "ѕαѕυкє",
                color = PinterestRed.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar: Rounded pill-shaped, dark grey background (#1E1E1E)
        // Left search icon, right 3-dot menu. Placeholder: "Search or insert URL"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(DarkGreySurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        onSearchSubmitted(searchQuery)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("search_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search or insert URL",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchSubmitted(searchQuery)
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PinterestRed
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input")
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3-dot menu on the right
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("search_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu options",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkGreySurface)
                    ) {
                        val quickQueries = listOf(
                            "Sasuke",
                            "Sasuke Uchiha",
                            "Naruto & Sasuke",
                            "Anime Wallpaper 4K",
                            "Dark Aesthetic",
                            "Cyberpunk Samurai"
                        )

                        quickQueries.forEach { query ->
                            DropdownMenuItem(
                                text = { Text(query, color = TextWhite, fontSize = 14.sp) },
                                onClick = {
                                    showMenu = false
                                    onSearchQueryChanged(query)
                                    onQuickFilterSelected(query)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
