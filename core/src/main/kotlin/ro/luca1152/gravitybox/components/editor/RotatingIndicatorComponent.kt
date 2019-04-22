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

package ro.luca1152.gravitybox.components.editor

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Adds an indicator which shows that the object is a rotating one. */
class RotatingIndicatorComponent : Component, Poolable {
    var indicatorImage = Image()

    fun set(context: Context) {
        val gameStage: GameStage = context.inject()
        val manager: AssetManager = context.inject()
        indicatorImage = Image(manager.get(Assets.uiSkin).getDrawable("rotating-platform-indicator")).apply {
            setSize(prefWidth.pixelsToMeters, prefHeight.pixelsToMeters)
            setOrigin(width / 2f, height / 2f)
            touchable = Touchable.disabled
        }
        gameStage.addActor(indicatorImage)
    }

    override fun reset() {
        indicatorImage.remove()
        indicatorImage = Image()
    }

    companion object : ComponentResolver<RotatingIndicatorComponent>(RotatingIndicatorComponent::class.java)
}

val Entity.rotatingIndicator: RotatingIndicatorComponent
    get() = RotatingIndicatorComponent[this]

fun Entity.rotatingIndicator(context: Context) =
    add(createComponent<RotatingIndicatorComponent>(context).apply {
        set(context)
    })!!