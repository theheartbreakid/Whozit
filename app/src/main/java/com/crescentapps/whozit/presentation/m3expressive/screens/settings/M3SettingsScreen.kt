package com.crescentapps.whozit.presentation.m3expressive.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.whozit.core.model.ThemeMode
import com.crescentapps.whozit.core.model.UiSystemMode
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveCard
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveLivePreview
import com.crescentapps.whozit.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.whozit.presentation.screens.settings.SettingsViewModel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import com.crescentapps.whozit.core.update.model.UpdateState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3SettingsScreen(viewModel: SettingsViewModel) {
    val prefs by viewModel.preferences.collectAsState(
        initial = com.crescentapps.whozit.data.preferences.UserPreferences()
    )
    val updateState by viewModel.updateState.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = com.crescentapps.whozit.presentation.theme.whozitScaffoldWindowInsets(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "About Whozit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = ExpressiveTokens.spacing.normal,
                end = ExpressiveTokens.spacing.normal,
                top = ExpressiveTokens.spacing.small,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card
            item {
                WhozitExpressiveLivePreview()
            }

            // Updates Section
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val lastCheckedText = if (prefs.lastUpdateCheckTimestamp > 0) {
                            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            "Last checked: " + sdf.format(Date(prefs.lastUpdateCheckTimestamp))
                        } else {
                            "Not checked yet"
                        }

                        val checkingSubtitle = when (updateState) {
                            is UpdateState.Checking -> "Checking for updates..."
                            is UpdateState.UpToDate -> "You're up to date · $lastCheckedText"
                            is UpdateState.UpdateAvailable -> "Update available! · $lastCheckedText"
                            is UpdateState.Downloading -> "Downloading update..."
                            else -> lastCheckedText
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (updateState is UpdateState.Checking) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Outlined.SystemUpdate, null, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text("Check for Updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(checkingSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            FilledTonalButton(
                                onClick = { viewModel.checkForUpdates() }
                            ) {
                                Text(
                                    if (updateState is UpdateState.Checking) "Checking..." else "Check Now",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // UI Style Selector
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Appearance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "UI Style",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose the visual rendering engine for the app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val modes = listOf(
                            UiSystemMode.GAUSSIAN_BLUR to "Gaussian",
                            UiSystemMode.LIQUID to "Liquid",
                            UiSystemMode.MATERIAL_3_EXPRESSIVE to "Expressive"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            modes.forEachIndexed { index, (mode, label) ->
                                val isSelected = prefs.uiSystemMode == mode
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setUiSystemMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Theme Mode
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val themeModes = listOf(
                            ThemeMode.SYSTEM to "System",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.DARK to "Dark"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            themeModes.forEachIndexed { index, (mode, label) ->
                                val isSelected = prefs.themeMode == mode
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themeModes.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Color Personalities & Dynamics
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Color Personalities",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val personalities = listOf(
                            "VIBRANT" to "Vibrant",
                            "BALANCED" to "Balanced",
                            "CALM" to "Calm",
                            "PLAYFUL" to "Playful",
                            "ELEGANT" to "Elegant",
                            "MONOCHROME" to "Mono",
                            "NATURE" to "Nature"
                        )

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            personalities.forEach { (pKey, label) ->
                                val isSelected = prefs.m3ColorPersonality == pKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateM3ColorPersonality(pKey) },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Intensity Slider
                        var sliderValue by remember(prefs.m3ColorIntensity) { mutableFloatStateOf(prefs.m3ColorIntensity) }
                        Text(
                            text = "Color Intensity",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Expressive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { viewModel.updateM3ColorIntensity(sliderValue) },
                            valueRange = 0f..1f
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Use wallpaper colors for the theme")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.m3DynamicColor,
                                    onCheckedChange = { viewModel.updateM3DynamicColor(it) }
                                )
                            }
                        ) {
                            Text("Dynamic Color", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Typography & Emphasis
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.TextFields, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Typography & Font",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Font Family",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val fontFamilies = listOf(
                            "SYSTEM" to "System",
                            "GOOGLE_SANS" to "Sans",
                            "SF_PRO" to "SF Pro",
                            "SF_PRO_ROUNDED" to "Rounded"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            fontFamilies.forEachIndexed { index, (fontKey, label) ->
                                val isSelected = prefs.m3TypographyStyle == fontKey
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateM3TypographyStyle(fontKey) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = fontFamilies.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Typography Emphasis",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val emphases = listOf(
                            "STANDARD" to "Standard",
                            "EXPRESSIVE" to "Expressive",
                            "STRONG" to "Strong"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            emphases.forEachIndexed { index, (empKey, label) ->
                                val isSelected = prefs.m3TypographyEmphasis == empKey
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateM3TypographyEmphasis(empKey) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = emphases.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Density & Shape
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Density & Shapes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Layout Density",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val densities = listOf(
                            "COMFORTABLE" to "Comfortable",
                            "STANDARD" to "Standard",
                            "COMPACT" to "Compact"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            densities.forEachIndexed { index, (dKey, label) ->
                                val isSelected = prefs.m3Density == dKey
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateM3Density(dKey) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = densities.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Shape Personality",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val shapes = listOf(
                            "EXPRESSIVE" to "Expressive",
                            "ROUNDED" to "Rounded",
                            "BALANCED" to "Balanced",
                            "STRUCTURED" to "Structured"
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 40.dp)
                        ) {
                            shapes.forEachIndexed { index, (sKey, label) ->
                                val isSelected = prefs.m3ShapePersonality == sKey
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateM3ShapePersonality(sKey) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = shapes.size),
                                    icon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Motion & Reset
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Motion & System",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Use bouncy spatial springs and morphing transitions")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.m3UseExpressiveMotion,
                                    onCheckedChange = { viewModel.updateM3UseExpressiveMotion(it) }
                                )
                            }
                        ) {
                            Text("Expressive Motion", fontWeight = FontWeight.SemiBold)
                        }

                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Allow expressive shape transformations")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.m3ShapeMorphingEnabled,
                                    onCheckedChange = { viewModel.setM3ShapeMorphingEnabled(it) }
                                )
                            }
                        ) {
                            Text("Shape Morphing", fontWeight = FontWeight.SemiBold)
                        }
                        
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Allow non-essential animated visual effects")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.m3DecorativeMotionEnabled,
                                    onCheckedChange = { viewModel.setM3DecorativeMotionEnabled(it) }
                                )
                            }
                        ) {
                            Text("Decorative Motion", fontWeight = FontWeight.SemiBold)
                        }

                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Minimize expressive and decorative movement")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.isReduceMotion,
                                    onCheckedChange = { viewModel.setReduceMotion(it) }
                                )
                            }
                        ) {
                            Text("Reduce Motion", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { viewModel.resetAppearanceSettings() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Appearance Defaults", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gradient Fills & Light Rays
            item {
                WhozitExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Gradient Fills",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            supportingContent = {
                                Text("Configurable directional gradient light wash")
                            },
                            trailingContent = {
                                Switch(
                                    checked = prefs.m3LightRaysEnabled,
                                    onCheckedChange = { viewModel.setM3LightRaysEnabled(it) }
                                )
                            }
                        ) {
                            Text("Gradient Fills", fontWeight = FontWeight.SemiBold)
                        }

                        if (prefs.m3LightRaysEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Gradient Source",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val sources = listOf(
                                "PRIMARY" to "Primary",
                                "SECONDARY" to "Secondary",
                                "TERTIARY" to "Tertiary"
                            )

                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                sources.forEachIndexed { index, (key, label) ->
                                    val isSelected = prefs.m3GradientColorSource == key
                                    SegmentedButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setM3GradientColorSource(key) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sources.size),
                                        icon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Presets", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        viewModel.setM3GradientX(0f)
                                        viewModel.setM3GradientY(0f)
                                        viewModel.setM3GradientAngle(45f)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) { Text("Top Left", fontSize = 12.sp) }
                                
                                Button(
                                    onClick = { 
                                        viewModel.setM3GradientX(1f)
                                        viewModel.setM3GradientY(0f)
                                        viewModel.setM3GradientAngle(135f)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) { Text("Top Right", fontSize = 12.sp) }
                                
                                Button(
                                    onClick = { 
                                        viewModel.setM3GradientX(0.5f)
                                        viewModel.setM3GradientY(0f)
                                        viewModel.setM3GradientAngle(90f)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) { Text("Top Down", fontSize = 12.sp) }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            var sliderX by remember(prefs.m3GradientX) { mutableFloatStateOf(prefs.m3GradientX) }
                            Text("Light Position X", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderX,
                                onValueChange = { sliderX = it },
                                onValueChangeFinished = { viewModel.setM3GradientX(sliderX) },
                                valueRange = 0f..1f
                            )

                            var sliderY by remember(prefs.m3GradientY) { mutableFloatStateOf(prefs.m3GradientY) }
                            Text("Light Position Y", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderY,
                                onValueChange = { sliderY = it },
                                onValueChangeFinished = { viewModel.setM3GradientY(sliderY) },
                                valueRange = 0f..1f
                            )

                            var sliderAngle by remember(prefs.m3GradientAngle) { mutableFloatStateOf(prefs.m3GradientAngle) }
                            Text("Direction (°)", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderAngle,
                                onValueChange = { sliderAngle = it },
                                onValueChangeFinished = { viewModel.setM3GradientAngle(sliderAngle) },
                                valueRange = 0f..360f
                            )

                            var sliderWidth by remember(prefs.m3GradientWidth) { mutableFloatStateOf(prefs.m3GradientWidth) }
                            Text("Width", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderWidth,
                                onValueChange = { sliderWidth = it },
                                onValueChangeFinished = { viewModel.setM3GradientWidth(sliderWidth) },
                                valueRange = 0f..1f
                            )

                            var sliderLength by remember(prefs.m3GradientLength) { mutableFloatStateOf(prefs.m3GradientLength) }
                            Text("Length", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderLength,
                                onValueChange = { sliderLength = it },
                                onValueChangeFinished = { viewModel.setM3GradientLength(sliderLength) },
                                valueRange = 0f..2f
                            )

                            var sliderSoftness by remember(prefs.m3GradientSoftness) { mutableFloatStateOf(prefs.m3GradientSoftness) }
                            Text("Softness", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderSoftness,
                                onValueChange = { sliderSoftness = it },
                                onValueChangeFinished = { viewModel.setM3GradientSoftness(sliderSoftness) },
                                valueRange = 0f..1f
                            )

                            var sliderIntensity by remember(prefs.m3GradientIntensity) { mutableFloatStateOf(prefs.m3GradientIntensity) }
                            Text("Intensity", style = MaterialTheme.typography.titleMedium)
                            Slider(
                                value = sliderIntensity,
                                onValueChange = { sliderIntensity = it },
                                onValueChangeFinished = { viewModel.setM3GradientIntensity(sliderIntensity) },
                                valueRange = 0f..1f
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        M3AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    when (val state = updateState) {
        is UpdateState.UpToDate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Up to Date") },
                text = { Text("You're already using the latest version of Whozit.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("OK")
                    }
                }
            )
        }
        is UpdateState.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Update Available") },
                text = { Text("A new version of Whozit is available.\n\nVersion: ${state.updateInfo.versionName}\n\n${state.updateInfo.releaseNotes}") },
                confirmButton = {
                    Button(onClick = { viewModel.startDownload(state.updateInfo) }) {
                        Text("Download & Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Later")
                    }
                }
            )
        }
        is UpdateState.Downloading -> {
            val progressPercent = (state.progress * 100).toInt()
            val downloadedMb = state.downloadedBytes / (1024f * 1024f)
            val totalMb = state.totalBytes / (1024f * 1024f)
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Downloading Update...") },
                text = { Text("Progress: $progressPercent%\n%.2f MB / %.2f MB".format(downloadedMb, totalMb)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.cancelDownload() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        is UpdateState.WaitingForInstallPermission -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Install Permission Required") },
                text = { Text("Whozit downloaded the update, but Android requires you to allow Whozit to install applications from this source.\n\nEnable 'Allow from this source' for Whozit to continue the update.") },
                confirmButton = {
                    Button(onClick = {
                        val intent = viewModel.createManageUnknownAppSourcesIntent()
                        if (intent != null) {
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        is UpdateState.ReadyToInstall -> {
            val updateManager = remember { com.crescentapps.whozit.core.update.UpdateManager.getInstance(context) }
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Update Downloaded") },
                text = { Text("The update is ready to be installed. Tap 'Install' to continue.") },
                confirmButton = {
                    Button(onClick = { updateManager.installApk(state.apkFile, state.updateInfo) }) {
                        Text("Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Later")
                    }
                }
            )
        }
        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Update Error") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = {
                        if (state.canRetry) {
                            viewModel.checkForUpdates()
                        } else {
                            viewModel.dismissUpdateDialog()
                        }
                    }) {
                        Text(if (state.canRetry) "Retry" else "OK")
                    }
                },
                dismissButton = {
                    if (state.canRetry) {
                        TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
        else -> {}
    }
}
