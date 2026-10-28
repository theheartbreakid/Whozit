package com.crescentapps.whozit.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

val LocalTextOutlineEnabled = compositionLocalOf { false }

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = TextStyle.Default
) {
    val finalColor = if (color == Color.Unspecified) com.crescentapps.whozit.presentation.theme.LocalWhozitColors.current.textPrimary else color
    val isOutlineEnabled = LocalTextOutlineEnabled.current

    val mergedStyle = style.merge(
        TextStyle(
            color = finalColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            textAlign = textAlign,
            lineHeight = lineHeight
        )
    )

    if (isOutlineEnabled && text.isNotBlank()) {
        val contrastOutlineColor = if (finalColor.luminance() < 0.5f) {
            Color.White.copy(alpha = 0.65f)
        } else {
            Color.Black.copy(alpha = 0.65f)
        }

        Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
            BasicText(
                text = text,
                style = mergedStyle.copy(
                    drawStyle = Stroke(
                        width = 1.5f,
                        join = StrokeJoin.Round
                    ),
                    color = contrastOutlineColor
                ),
                maxLines = maxLines,
                overflow = overflow
            )
            BasicText(
                text = text,
                style = mergedStyle,
                maxLines = maxLines,
                overflow = overflow
            )
        }
    } else {
        BasicText(
            text = text,
            modifier = modifier,
            style = mergedStyle,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val finalTint = if (tint == Color.Unspecified) com.crescentapps.whozit.presentation.theme.LocalWhozitColors.current.iconTint else tint
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(finalTint)
    )
}
