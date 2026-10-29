package com.crescentapps.whozit.presentation.components.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.crescentapps.whozit.WhozitApplication
import com.crescentapps.whozit.presentation.catalog.utils.DampedDragAnimation
import com.crescentapps.whozit.presentation.catalog.utils.InteractiveHighlight
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.crescentapps.whozit.presentation.components.AdaptiveLuminanceProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private val DummyRowScope = object : RowScope {
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
    override fun Modifier.align(alignment: Alignment.Vertical): Modifier = this
    override fun Modifier.alignBy(alignmentLine: androidx.compose.ui.layout.HorizontalAlignmentLine): Modifier = this
    override fun Modifier.alignBy(alignmentLineBlock: (androidx.compose.ui.layout.Measured) -> Int): Modifier = this
    override fun Modifier.alignByBaseline(): Modifier = this
}

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    accentColor: Color = if (isSystemInDarkTheme()) Color(0xFF0091FF) else Color(0xFF0088FF),
    content: @Composable RowScope.() -> Unit
) {
    val whozitColors = com.crescentapps.whozit.presentation.theme.LocalWhozitColors.current
    val isLightTheme = whozitColors.isLight
    val containerColor =
        if (isLightTheme) Color(0xFFFAFAFA).copy(0.4f)
        else Color(0xFF121212).copy(0.4f)

    val tabsBackdrop = rememberLayerBackdrop()
    val layer = rememberGraphicsLayer()
    val dockSettings = com.crescentapps.whozit.presentation.components.LocalDockSettings.current
    val context = LocalContext.current
    val hapticManager = remember(context) {
        (context.applicationContext as WhozitApplication).hapticManager
    }

    AdaptiveLuminanceProvider(layer = layer, enabled = true) {
        BoxWithConstraints(
            modifier,
            contentAlignment = Alignment.CenterStart
        ) {
        val density = LocalDensity.current
        val tabSize = with(density) {
            if (isVertical) {
                (constraints.maxHeight.toFloat() - 8f.dp.toPx()) / tabsCount
            } else {
                (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount
            }
        }
        val currentTabSize by rememberUpdatedState(tabSize)

        val offsetAnimation = remember { Animatable(0f) }
        val maxExtent = if (isVertical) constraints.maxHeight.toFloat() else constraints.maxWidth.toFloat()
        val panelOffset by remember(density, isVertical) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / maxExtent).fastCoerceIn(-1f, 1f)
                with(density) {
                    4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember {
            mutableIntStateOf(selectedTabIndex())
        }
        val dampedDragAnimation = remember(animationScope, tabsCount, isVertical) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTabIndex().toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    if (currentIndex == targetIndex) {
                        animateToValue(targetIndex.toFloat())
                    } else {
                        currentIndex = targetIndex
                    }
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    val size = currentTabSize
                    val delta = if (isVertical) dragAmount.y else (dragAmount.x * if (isLtr) 1f else -1f)
                    updateValue(
                        (targetValue + delta / size)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    )
                    val dragDelta = if (isVertical) dragAmount.y else dragAmount.x
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragDelta)
                    }
                }
            )
        }
        LaunchedEffect(selectedTabIndex) {
            snapshotFlow { selectedTabIndex() }
                .collectLatest { index ->
                    if (currentIndex != index) {
                        currentIndex = index
                    }
                }
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentIndex }
                .drop(1) // Avoid haptic on initial composition
                .collectLatest { index ->
                    hapticManager.vibrateSubtle()
                    dampedDragAnimation.animateToValue(index.toFloat())
                    onTabSelected(index)
                }
        }

        val interactiveHighlight = remember(animationScope, isVertical) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, offset ->
                    if (isVertical) {
                        Offset(
                            size.width / 2f,
                            (dampedDragAnimation.value + 0.5f) * tabSize + panelOffset
                        )
                    } else {
                        Offset(
                            if (isLtr) (dampedDragAnimation.value + 0.5f) * tabSize + panelOffset
                            else size.width - (dampedDragAnimation.value + 0.5f) * tabSize + panelOffset,
                            size.height / 2f
                        )
                    }
                }
            )
        }

        val baseModifier = Modifier
            .graphicsLayer {
                if (isVertical) {
                    translationY = panelOffset
                } else {
                    translationX = panelOffset
                }
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(dockSettings.cornerRadius.dp) },
                effects = {
                    vibrancy()
                    blur(dockSettings.blurRadius.dp.toPx())
                    liquidLens(
                        refractionHeight = dockSettings.refractionHeight.dp.toPx(),
                        refractionAmount = dockSettings.refractionAmount.dp.toPx(),
                        chromaticAberration = dockSettings.chromaticAberration
                    )
                },
                layerBlock = {
                    val progress = dampedDragAnimation.pressProgress
                    val scale = lerp(1f, 1f + 16f.dp.toPx() / (if (isVertical) size.height else size.width), progress)
                    scaleX = scale
                    scaleY = scale
                },
                onDrawBackdrop = { drawBackdrop ->
                    drawBackdrop()
                    layer.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                    ) { drawBackdrop() }
                },
                onDrawSurface = { drawRect(containerColor) }
            )
            .then(interactiveHighlight.modifier)

        val tabItemHeightDp = with(density) {
            ((constraints.maxHeight.toFloat() - 8f.dp.toPx()) / tabsCount).toDp()
        }

        val verticalRowScope = remember(tabsCount, tabItemHeightDp) {
            object : RowScope {
                override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this.fillMaxWidth().height(tabItemHeightDp)
                override fun Modifier.align(alignment: Alignment.Vertical): Modifier = this
                override fun Modifier.alignBy(alignmentLine: androidx.compose.ui.layout.HorizontalAlignmentLine): Modifier = this
                override fun Modifier.alignBy(alignmentLineBlock: (androidx.compose.ui.layout.Measured) -> Int): Modifier = this
                override fun Modifier.alignByBaseline(): Modifier = this
            }
        }

        if (isVertical) {
            Column(
                modifier = baseModifier
                    .width(68.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                verticalRowScope.content()
            }
        } else {
            Row(
                modifier = baseModifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }

        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides {
                lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
            }
        ) {
            val tabOverlayModifier = Modifier
                .clearAndSetSemantics {}
                .alpha(0f)
                .layerBackdrop(tabsBackdrop)
                .graphicsLayer {
                    if (isVertical) {
                        translationY = panelOffset
                    } else {
                        translationX = panelOffset
                    }
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(dockSettings.cornerRadius.dp) },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        vibrancy()
                        blur(dockSettings.blurRadius.dp.toPx())
                        liquidLens(
                            refractionHeight = dockSettings.refractionHeight.dp.toPx() * progress,
                            refractionAmount = dockSettings.refractionAmount.dp.toPx() * progress,
                            chromaticAberration = dockSettings.chromaticAberration
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Default.copy(alpha = progress)
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .then(interactiveHighlight.modifier)
                .graphicsLayer(colorFilter = ColorFilter.tint(accentColor))

            if (isVertical) {
                Column(
                    modifier = tabOverlayModifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    verticalRowScope.content()
                }
            } else {
                Row(
                    modifier = tabOverlayModifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                }
            }
        }

        Box(
            Modifier
                .padding(if (isVertical) PaddingValues(vertical = 4.dp) else PaddingValues(horizontal = 4.dp))
                .graphicsLayer {
                    if (isVertical) {
                        translationY = dampedDragAnimation.value * tabSize + panelOffset
                    } else {
                        translationX =
                            if (isLtr) dampedDragAnimation.value * tabSize + panelOffset
                            else size.width - (dampedDragAnimation.value + 1f) * tabSize + panelOffset
                    }
                }
                .then(interactiveHighlight.gestureModifier)
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { RoundedRectangle(dockSettings.cornerRadius.dp) },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        liquidLens(
                            10f.dp.toPx() * progress,
                            14f.dp.toPx() * progress,
                            chromaticAberration = dockSettings.chromaticAberration
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Default.copy(alpha = progress)
                    },
                    shadow = {
                        val progress = dampedDragAnimation.pressProgress
                        Shadow(alpha = progress)
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = 8f.dp * progress,
                            alpha = progress
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(
                            if (isLightTheme) Color.Black.copy(0.1f)
                            else Color.White.copy(0.1f),
                            alpha = 1f - progress
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
                .then(if (isVertical) Modifier.width(56.dp).height(with(density) { tabSize.toDp() }) else Modifier.height(56.dp).fillMaxWidth(1f / tabsCount))
        )
    }
}
}
