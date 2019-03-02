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
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.utils.components.ComponentResolver

/** Indicates that the entity is a finish point. */
class FinishComponent : Component, Poolable {
    private var blinkEndlessly = true

    fun set(blinkEndlessly: Boolean = true, finishImage: ImageComponent) {
        this.blinkEndlessly = blinkEndlessly
        if (blinkEndlessly)
            addPermanentFadeInFadeOutActions(finishImage)
    }

    fun addPermanentFadeInFadeOutActions(image: ImageComponent) {
        image.img.addAction(RepeatAction().apply {
            action = Actions.sequence(
                Actions.fadeOut(1f),
                Actions.fadeIn(1f)
            )
            count = RepeatAction.FOREVER
        })
    }

    override fun reset() {
        blinkEndlessly = true
    }

    companion object : ComponentResolver<FinishComponent>(FinishComponent::class.java)
}

val Entity.finish: FinishComponent
    get() = FinishComponent[this]