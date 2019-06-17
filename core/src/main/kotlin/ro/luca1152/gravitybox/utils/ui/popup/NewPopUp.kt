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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.graphics.copy
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.UIViewport

/** A redesigned [PopUp]. */
open class NewPopUp(
    context: Context,
    width: Float, height: Float,
    skin: Skin
) : Group() {
    // Injected objects
    private val uiViewport: UIViewport = context.inject()
    private val manager: AssetManager = context.inject()
    private val uiStage: UIStage = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()

    /** What happens with the pop-up when the back button is pressed. */
    var backButtonRunnable = Runnable {
        hide()
    }

    private val whitePopUp = Image(skin.getDrawable("pop-up")).apply {
        setSize(width, height)
        setOrigin(width / 2f, height / 2f)
        color = Color.WHITE.copy(alpha = 0.85f)
    }

    /** Blocks touches outside the pop-up. */
    private val touchableImage1 = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        color.a = 0f
        setSize(uiViewport.worldWidth, uiViewport.worldHeight)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }
    private val touchableImage2 = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        color.a = 0f
        setSize(uiViewport.worldWidth, uiViewport.worldHeight)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }

    val widget = Table(skin).apply {
        addActor(whitePopUp.apply { toBack() })
        setSize(width, height)
        setPosition(uiViewport.worldWidth / 2f - width / 2f, uiViewport.worldHeight / 2f - height / 2f)
        pad(38f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }

    init {
        @Suppress("LeakingThis")
        addActor(widget)
        name = "NewPopUp"
    }

    fun hide(fadeOutDuration: Float = .15f) {
        addAction(
            Actions.sequence(
                Actions.fadeOut(fadeOutDuration),
                Actions.removeActor(),
                Actions.fadeIn(0f)
            )
        )
    }

    private fun show() {
        addAction(
            Actions.sequence(
                Actions.fadeOut(0f),
                Actions.fadeIn(.15f)
            )
        )
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage != null) {
            uiStage.addActor(touchableImage1)
            menuOverlayStage.addActor(touchableImage2)
            show()
            widget.toFront()
            toFront()
        } else {
            touchableImage1.remove()
            touchableImage2.remove()
        }
    }
}