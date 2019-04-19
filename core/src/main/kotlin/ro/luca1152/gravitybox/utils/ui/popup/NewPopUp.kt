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
import ro.luca1152.gravitybox.utils.assets.Assets
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** A redesigned [PopUp]. */
class NewPopUp(
    width: Float, height: Float,
    skin: Skin,
    manager: AssetManager = Injekt.get()
) : Group() {
    private val whitePopUp = Image(skin.getDrawable("pop-up")).apply {
        setSize(width, height)
        setOrigin(width / 2f, height / 2f)
        color = Color.WHITE.copy(alpha = 0.85f)
    }

    /** Blocks touches outside the pop-up. */
    private val touchableImage = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        color.a = 0f
        setSize(720f, 1280f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }

    val widget = Table(skin).apply {
        addActor(whitePopUp.apply { toBack() })
        setSize(width, height)
        setPosition(720f / 2f - width / 2f, 1280f / 2f - height / 2f)
        pad(38f)
    }

    init {
        addActor(touchableImage)
        addActor(widget)
    }

    fun hide() {
        addAction(
            Actions.sequence(
                Actions.fadeOut(.15f),
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
            show()
        }
    }
}