package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.graphics.Color

object ColorScheme {
    // The hue of the first level
    private const val initialHue = 180

    // The 4 colors used in the game
    var lightColor = getLightColor(initialHue)
    var darkColor = getDarkColor(initialHue)
    var lightColor2 = getLightColor2(initialHue)
    var darkColor2 = getDarkColor2(initialHue)

    fun getLightColor(hue: Int): Color {
        val color = Color().fromHsv(hue.toFloat(), 10f / 100f, 91f / 100f)
        color.a = 1f
        return color
    }

    fun getDarkColor(hue: Int): Color {
        val color = Color().fromHsv(hue.toFloat(), 42f / 100f, 57f / 100f)
        color.a = 1f
        return color
    }

    fun getLightColor2(hue: Int): Color {
        val color = Color().fromHsv(hue.toFloat(), 94f / 100f, 20f / 100f)
        color.a = 1f
        return color
    }

    fun getDarkColor2(hue: Int): Color {
        val color = Color().fromHsv(hue.toFloat(), 85f / 100f, 95f / 100f)
        color.a = 1f
        return color
    }
}