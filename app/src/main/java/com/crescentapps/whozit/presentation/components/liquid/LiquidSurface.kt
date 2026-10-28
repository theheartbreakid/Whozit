package com.crescentapps.whozit.presentation.components.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.BackdropEffectScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.crescentapps.whozit.presentation.components.LocalGlassSettings
import com.crescentapps.whozit.presentation.components.AdaptiveLuminanceProvider
import com.crescentapps.whozit.presentation.components.LocalAdaptiveLuminanceActive
import com.crescentapps.whozit.presentation.components.LocalGlassIntensity
import com.crescentapps.whozit.presentation.components.PrismalSurface

@Composable
fun LiquidSurface(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape? = null,
    tint: Color = Color.Unspecified,
    adaptiveLuminance: Boolean = true,
    content: @Composable () -> Unit
) {
    if (backdrop == null) {
        PrismalSurface(
            modifier = modifier,
            shape = shape ?: RoundedCornerShape(24.dp),
            tonalColor = if (tint == Color.Unspecified) null else tint,
            adaptiveLuminance = adaptiveLuminance,
            content = content
        )
        return
    }

    val glassSettings = LocalGlassSettings.current
    val glassIntensity = LocalGlassIntensity.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    val actualShape = remember(shape, glassSettings.cornerRadius) {
        shape ?: RoundedCornerShape(glassSettings.cornerRadius.dp)
    }
    val isLuminanceRoot = !LocalAdaptiveLuminanceActive.current && adaptiveLuminance
    val layer = if (isLuminanceRoot) rememberGraphicsLayer() else null

    val blurPx = remember(density, glassSettings.blurRadius, glassIntensity) {
        with(density) { glassSettings.blurRadius.dp.toPx() } * glassIntensity
    }
    val refractionHeightPx = remember(density, glassSettings.refractionHeight, glassIntensity) {
        with(density) { glassSettings.refractionHeight.dp.toPx() } * glassIntensity
    }
    val refractionAmountPx = remember(density, glassSettings.refractionAmount, glassIntensity) {
        with(density) { glassSettings.refractionAmount.dp.toPx() } * glassIntensity
    }
    val isScrollInProgressProvider = com.crescentapps.whozit.presentation.components.LocalScrollInProgress.current

    val chromaticAberrationBase = remember(glassSettings.chromaticAberration, glassIntensity) {
        glassSettings.chromaticAberration * glassIntensity
    }

    val effectsBlock: BackdropEffectScope.() -> Unit = remember(
        blurPx,
        refractionHeightPx,
        refractionAmountPx,
        chromaticAberrationBase,
        isScrollInProgressProvider
    ) {
        {
            vibrancy()
            blur(blurPx)
            liquidLens(
                refractionHeight = refractionHeightPx,
                refractionAmount = refractionAmountPx,
                chromaticAberration = if (isScrollInProgressProvider()) 0f else chromaticAberrationBase
            )
        }
    }

    val onDrawBackdropBlock: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = remember(tint, isLuminanceRoot, layer, isScrollInProgressProvider) {
        val block: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = { drawBackdrop ->
            drawBackdrop()
            if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.1f))
            }
            if (isLuminanceRoot && !isScrollInProgressProvider()) {
                layer?.record(
                    density = this,
                    layoutDirection = layoutDirection,
                    size = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                ) {
                    drawBackdrop()
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.1f))
                    }
                }
            }
        }
        block
    }

    AdaptiveLuminanceProvider(layer = layer, enabled = isLuminanceRoot) {
        Box(
            modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { actualShape },
                    effects = effectsBlock,
                    onDrawBackdrop = onDrawBackdropBlock,
                    onDrawSurface = {
                        // Tint is now applied in onDrawBackdrop for true glass material rendering
                    }
                )
                .clip(actualShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}