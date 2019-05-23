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

@file:Suppress("MemberVisibilityCanBePrivate")

package ro.luca1152.gravitybox.utils.ui.popup

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickTextButton

open class YesNoPopUp(
    context: Context,
    width: Float, height: Float,
    skin: Skin,
    yesIsHighlighted: Boolean = false, noIsHighlighted: Boolean = false
) : PopUp(context, width, height, skin) {
    private val manager: AssetManager = context.inject()
    val emptySpace = Table()
    var yesClickRunnable: Runnable? = null
    var noClickRunnable: Runnable? = null
    private val bottomHeight = 106f
    private val topLine = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(width, borderThickness)
        color = Colors.gameColor
    }
    private val noButtonHighlight = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(width / 2f, bottomHeight + borderThickness)
        setPosition(-borderThickness, borderThickness / 2f)
        color = Colors.gameColor
    }
    private val noButton = ClickTextButton(context, "simple-button", skin, "NO", "regular", 75f, !noIsHighlighted).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            this@YesNoPopUp.remove()
            noClickRunnable?.run()
        }
    }
    private val middleLine = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(borderThickness, bottomHeight)
        color = Colors.gameColor
    }
    private val yesButtonHighlight = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(width / 2f, bottomHeight + borderThickness)
        setPosition(width / 2f - borderThickness, borderThickness / 2f)
        color = Colors.gameColor
    }
    private val yesButton = ClickTextButton(context, "simple-button", skin, "YES", "regular", 75f, !yesIsHighlighted).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
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
            noButton.upColor = Colors.bgColor
        }
        add(noButton).expandX().width(width / 2f - borderThickness / 2f - borderThickness)
        add(middleLine).size(borderThickness, bottomHeight).padBottom(-5f)
        if (yesIsHighlighted) {
            addActor(yesButtonHighlight)
            yesButton.upColor = Colors.bgColor
        }
        add(yesButton).width(width / 2f - borderThickness / 2f - borderThickness).expandX()
    }

    init {
        widget.run {
            add(emptySpace).height(height - bottomHeight - 3 * borderThickness).padTop(borderThickness).row()
            add(bottomRow)
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        topLine.color = Colors.gameColor
        noButtonHighlight.color = Colors.gameColor
        middleLine.color = Colors.gameColor
        yesButtonHighlight.color = Colors.gameColor
    }
}