package com.lu4p.fokuslauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lu4p.fokuslauncher.ui.theme.LocalPhotoWallpaperOutlineWidthDp

/**
 * Renders the clock's numeric characters as a compact 5×7 dot matrix. The caller should fall
 * back to normal text for locales whose formatted time contains unsupported characters.
 */
@Composable
fun DotMatrixText(
        text: String,
        style: TextStyle,
        color: Color,
        modifier: Modifier = Modifier,
        outlined: Boolean = false,
) {
    val density = LocalDensity.current
    val dotDiameter = with(density) { (style.fontSize * 0.095f).toDp() }.coerceAtLeast(3.dp)
    val dotGap = dotDiameter * 0.52f
    val characterGap = dotGap * 1.45f
    val glyphs = text.mapNotNull(::dotMatrixGlyph)
    val width =
            if (glyphs.isEmpty()) {
                0.dp
            } else {
                glyphs.fold(0.dp) { total, glyph ->
                    total + (dotDiameter + dotGap) * glyph.columns + characterGap
                } - characterGap
            }
    val lineHeight = with(density) { style.lineHeight.toDp() }.coerceAtLeast((dotDiameter + dotGap) * 7)
    val dotOutline = if (outlined) maxOf(1.dp, LocalPhotoWallpaperOutlineWidthDp.current.dp) else 0.dp

    Canvas(modifier = modifier.size(width, lineHeight)) {
        var x = 0f
        val dotDiameterPx = dotDiameter.toPx()
        val dotGapPx = dotGap.toPx()
        val characterGapPx = characterGap.toPx()
        val yInset = (size.height - (7 * dotDiameterPx + 6 * dotGapPx)) / 2f
        glyphs.forEach { glyph ->
            glyph.rows.forEachIndexed { row, columns ->
                columns.forEach { column ->
                    val center =
                            Offset(
                                    x = x + column * (dotDiameterPx + dotGapPx) + dotDiameterPx / 2,
                                    y = yInset + row * (dotDiameterPx + dotGapPx) + dotDiameterPx / 2,
                            )
                    if (dotOutline > 0.dp) {
                        drawCircle(Color.Black, dotDiameterPx / 2 + dotOutline.toPx(), center)
                    }
                    drawCircle(color, dotDiameterPx / 2, center)
                }
            }
            x += glyph.columns * (dotDiameterPx + dotGapPx) + characterGapPx
        }
    }
}

private data class DotMatrixGlyph(val columns: Int, val rows: List<IntArray>)

private fun dotMatrixGlyph(character: Char): DotMatrixGlyph? =
        when (character) {
            '0' -> glyph("01110", "10001", "10011", "10101", "11001", "10001", "01110")
            '1' -> glyph("00100", "01100", "00100", "00100", "00100", "00100", "01110")
            '2' -> glyph("01110", "10001", "00001", "00010", "00100", "01000", "11111")
            '3' -> glyph("11110", "00001", "00001", "01110", "00001", "00001", "11110")
            '4' -> glyph("00010", "00110", "01010", "10010", "11111", "00010", "00010")
            '5' -> glyph("11111", "10000", "10000", "11110", "00001", "00001", "11110")
            '6' -> glyph("01110", "10000", "10000", "11110", "10001", "10001", "01110")
            '7' -> glyph("11111", "00001", "00010", "00100", "01000", "01000", "01000")
            '8' -> glyph("01110", "10001", "10001", "01110", "10001", "10001", "01110")
            '9' -> glyph("01110", "10001", "10001", "01111", "00001", "00001", "01110")
            ':' -> DotMatrixGlyph(columns = 1, rows = listOf(intArrayOf(), intArrayOf(0), intArrayOf(0), intArrayOf(), intArrayOf(0), intArrayOf(0), intArrayOf()))
            else -> null
        }

private fun glyph(vararg rows: String): DotMatrixGlyph =
        DotMatrixGlyph(
                columns = rows.first().length,
                rows = rows.map { row -> row.mapIndexedNotNull { index, value -> index.takeIf { value == '1' } }.toIntArray() },
        )
