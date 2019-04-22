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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Adds a permanent fade in-fade out effect. */
class FadeInFadeOutComponent : Component, Poolable {
    private lateinit var scene2D: Scene2DComponent
    private var fadeInFadeOutAction: Action? = null

    fun set(scene2D: Scene2DComponent, delayBeforeStarting: Float = 0f) {
        this.scene2D = scene2D
        addPermanentFadeInFadeOut(delayBeforeStarting)
    }

    private fun addPermanentFadeInFadeOut(delayBeforeStarting: Float) {
        fadeInFadeOutAction = SequenceAction(
            Actions.delay(delayBeforeStarting),
            Actions.fadeIn(1f - scene2D.color.a),
            Actions.repeat(
                RepeatAction.FOREVER,
                Actions.sequence(
                    Actions.fadeOut(1f),
                    Actions.fadeIn(1f)
                )
            )
        )
        scene2D.group.addAction(fadeInFadeOutAction)
    }

    override fun reset() {
        scene2D.group.removeAction(fadeInFadeOutAction)
        scene2D.color.a = 1f
        fadeInFadeOutAction = null
    }

    companion object : ComponentResolver<FadeInFadeOutComponent>(FadeInFadeOutComponent::class.java)
}

fun Entity.fadeInFadeOut(
    context: Context,
    scene2D: Scene2DComponent,
    delayBeforeStarting: Float = 0f
) =
    add(createComponent<FadeInFadeOutComponent>(context).apply {
        set(scene2D, delayBeforeStarting)
    })!!