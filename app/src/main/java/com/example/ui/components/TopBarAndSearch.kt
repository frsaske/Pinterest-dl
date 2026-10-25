package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkGreySurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextWhite
import com.example.util.VibrationHelper

/**
 * Top bar matching the exact YTDLnis header style.
 * Displays "Pinterest" in crisp bold white, "DL" in secondary white.
 * Pill-shaped search bar with live YouTube-style autocomplete suggestions.
 */
@Composable
fun PinterestTopBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onQuickFilterSelected: (String) -> Unit,
    suggestions: List<String> = emptyList(),
    isSuggestionsVisible: Boolean = false,
    onSuggestionClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .zIndex(20f)
    ) {
        // Logo Header: High contrast monochrome YTDLnis typography
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.4).sp
                        )
                    ) {
                        append("Pinterest")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal,
                            fontSize = 22.sp
                        )
                    ) {
                        append("DL")
                    }
                },
                modifier = Modifier.testTag("app_logo")
            )

            // Stylized Developer Watermark (Clean monochrome)
            Text(
                text = "ѕαѕυкє",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                letterSpacing = 1.sp
            )
        }

        // Pill-shaped Search Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = if (isSuggestionsVisible && suggestions.isNotEmpty()) {
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    } else {
                        CircleShape
                    },
                    color = DarkGreySurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    VibrationHelper.vibrateClick(context, 25L)
                                    focusManager.clearFocus()
                                    onSearchSubmitted(searchQuery)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("search_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    text = "Search or insert URL",
                                    color = TextMuted,
                                    fontSize = 14.5.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        VibrationHelper.vibrateClick(context, 25L)
                                        focusManager.clearFocus()
                                        onSearchSubmitted(searchQuery)
                                    }
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
                                cursorColor = TextWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_input")
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    VibrationHelper.vibrateClick(context, 20L)
                                    onSearchQueryChanged("")
                                },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                // YouTube-Style Realtime Autocomplete Suggestions Dropdown
                AnimatedVisibility(
                    visible = isSuggestionsVisible && suggestions.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        color = Color(0xFF141418),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        shadowElevation = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            suggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            focusManager.clearFocus()
                                            VibrationHelper.vibrateClick(context, 25L)
                                            onSuggestionClicked(suggestion)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = suggestion,
                                            color = TextWhite,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.NorthWest,
                                        contentDescription = "Search suggestion",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
