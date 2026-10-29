package com.crescentapps.whozit.presentation.components.liquid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.whozit.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.whozit.presentation.navigation.Screen
import com.crescentapps.whozit.presentation.navigation.mainScreens
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors
import com.kyant.backdrop.Backdrop

/**
 * Dedicated, independent Liquid UI Landscape Side Dock.
 * Displays all six navigation destinations vertically in a compact,
 * unclipped liquid glass side panel with zero overlapping.
 */
@Composable
fun LiquidLandscapeDock(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val whozitColors = LocalWhozitColors.current
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val isDarkSurface = adaptiveColor.luminance() > 0.5f

    val selectedColor = if (isDarkSurface) {
        if (whozitColors.accent.luminance() < 0.4f) Color.White else whozitColors.accent
    } else {
        if (whozitColors.accent.luminance() > 0.65f) whozitColors.textPrimary else whozitColors.accent
    }
    val unselectedColor = whozitColors.textPrimary

    Box(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        LiquidSurface(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight(),
            backdrop = backdrop,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                mainScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val itemColor = if (isSelected) selectedColor else unselectedColor

                    val itemBackground = if (isSelected) {
                        whozitColors.accent.copy(alpha = 0.18f)
                    } else {
                        Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 2.dp, horizontal = 2.dp)
                            .clip(CircleShape)
                            .background(itemBackground)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { onNavigate(screen) }
                            )
                            .semantics {
                                role = Role.Tab
                                selected = isSelected
                                contentDescription = screen.title
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(itemColor)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            BasicText(
                                text = screen.title,
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = itemColor
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
