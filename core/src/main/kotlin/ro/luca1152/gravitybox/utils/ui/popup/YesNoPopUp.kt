/*
 * This file is part of Gravity Box.
 *
 * Gravity Box is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gravity Box is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gravity Box.  If not, see <https://www.gnu.org/licenses/>.
 */

package ro.luca1152.gravitybox.utils.ui.popup

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import ro.luca1152.gravitybox.utils.ui.button.ClickTextButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

open class YesNoPopUp(
    width: Float, height: Float,
    skin: Skin,
    yesIsHighlighted: Boolean = false, noIsHighlighted: Boolean = false,
    manager: AssetManager = Injekt.get()
) : PopUp(width, height, skin) {
    val emptySpace = Table()
    var yesClickRunnable: Runnable? = null
    var noClickRunnable: Runnable? = null
    private val bottomHeight = 106f
    private val topLine = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        setSize(width, borderThickness)
        color = ColorScheme.currentDarkColor
    }
    private val noButtonHighlight = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        setSize(width / 2f, bottomHeight + borderThickness)
        setPosition(-borderThickness, borderThickness / 2f)
        color = ColorScheme.currentDarkColor
    }
    private val noButton = ClickTextButton("simple-button", skin, "NO", "bold", 80f).apply {
        upColor = ColorScheme.currentDarkColor
        downColor = ColorScheme.darkerDarkColor
        clickRunnable = Runnable {
            this@YesNoPopUp.remove()
            noClickRunnable?.run()
        }
    }
    private val middleLine = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        setSize(borderThickness, bottomHeight)
        color = ColorScheme.currentDarkColor
    }
    private val yesButtonHighlight = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        setSize(width / 2f, bottomHeight + borderThickness)
        setPosition(width / 2f - borderThickness, borderThickness / 2f)
        color = ColorScheme.currentDarkColor
    }
    private val yesButton = ClickTextButton("simple-button", skin, "YES", "bold", 80f).apply {
        upColor = ColorScheme.currentDarkColor
        downColor = ColorScheme.darkerDarkColor
        clickRunnable = Runnable {
            this@YesNoPopUp.remove()
            yesClickRunnable?.run()
        }
    }
    private val bottomRow = Table().apply {
        padBottom(borderThickness)
        add(topLine).growX().height(borderThickness).top().colspan(3).padLeft(-5f).padRight(-5f).row()
        if (noIsHighlighted) {
            addActor(noButtonHighlight)
            noButton.upColor = ColorScheme.currentLightColor
        }
        add(noButton).expandX().width(width / 2f - borderThickness / 2f - borderThickness)
        add(middleLine).size(borderThickness, bottomHeight).padBottom(-5f)
        if (yesIsHighlighted) {
            addActor(yesButtonHighlight)
            yesButton.upColor = ColorScheme.currentLightColor
        }
        add(yesButton).width(width / 2f - borderThickness / 2f - borderThickness).expandX()
    }

    init {
        widget.run {
            add(emptySpace).height(height - bottomHeight - 3 * borderThickness).padTop(borderThickness).row()
            add(bottomRow)
        }
    }
}