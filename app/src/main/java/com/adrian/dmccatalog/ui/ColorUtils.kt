package com.adrian.dmccatalog.ui

import androidx.compose.ui.graphics.Color

fun hexToColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

fun bestContrastTextColor(backgroundHex: String): Color {
    val color = android.graphics.Color.parseColor(backgroundHex)
    val r = android.graphics.Color.red(color) / 255.0
    val g = android.graphics.Color.green(color) / 255.0
    val b = android.graphics.Color.blue(color) / 255.0
    val luma = 0.2126 * linearize(r) + 0.7152 * linearize(g) + 0.0722 * linearize(b)
    return if (luma > 0.5) Color.Black else Color.White
}

private fun linearize(channel: Double): Double {
    return if (channel <= 0.03928) channel / 12.92 else Math.pow((channel + 0.055) / 1.055, 2.4)
}
