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
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.graphics.copy
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton

open class PopUp(
    context: Context,
    width: Float, height: Float,
    skin: Skin
) : Group() {
    private val manager: AssetManager = context.inject()
    private val uiStage: UIStage = context.inject()

    val borderThickness = 14f
    private val screenTransparentBackground = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(uiStage.viewport.worldWidth, uiStage.viewport.worldHeight)
        color = Colors.bgColor.copy(alpha = .4f)
    }
    private val closeButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-x-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
    }
    private val widgetFrame = Image(skin.getDrawable("pop-up-frame")).apply {
        setSize(width, height)
        color = Colors.gameColor
    }
    private val widgetOpaqueBackground = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        setSize(width - 2 * borderThickness, height - 2 * borderThickness)
        color = Colors.bgColor
    }
    val widget = Table(skin).apply {
        setSize(width, height)
        setPosition(uiStage.viewport.worldWidth / 2f - width / 2f, uiStage.viewport.worldHeight / 2f - height / 2f)
        addActor(widgetOpaqueBackground.apply {
            setPosition(borderThickness, borderThickness)
        })
        addActor(widgetFrame)
        addActor(closeButton.apply {
            val additionalPadding = 5
            setPosition(width - this.width / 2f - additionalPadding, height - this.height / 2f - additionalPadding)
        })
        pad(borderThickness + 15f)
    }

    init {
        addListeners()
        addActors()
    }

    private fun addListeners() {
        addRunnableToCloseButton()
        addListenerToScreenBackground()
        addListenerToWidgetBackground()
    }

    private fun addRunnableToCloseButton() {
        closeButton.addClickRunnable(Runnable {
            remove()
        })
    }

    private fun addListenerToScreenBackground() {
        screenTransparentBackground.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if (isOver(screenTransparentBackground, x, y) && isOver(widgetOpaqueBackground, x, y)) {
                    remove()
                }
            }
        })
    }

    private fun addListenerToWidgetBackground() {
        widget.touchable = Touchable.enabled
        widget.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)
        screenTransparentBackground.color = Colors.bgColor
        screenTransparentBackground.color.a = 0.4f
        widgetFrame.color = Colors.gameColor
        widgetOpaqueBackground.color = Colors.bgColor
    }

    private fun addActors() {
        addActor(screenTransparentBackground)
        addActor(widget)
    }
}