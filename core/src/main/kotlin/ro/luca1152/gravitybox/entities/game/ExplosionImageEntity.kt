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

package ro.luca1152.gravitybox.entities.game

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object ExplosionImageEntity {
    fun createEntity(
        x: Float, y: Float,
        manager: AssetManager = Injekt.get(),
        engine: PooledEngine = Injekt.get()
    ) = engine.createEntity().apply {
        add(engine.createComponent(ExplosionComponent::class.java))
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.run {
                set(manager.getAsset("graphics/circle.png"), x, y)
                img.addAction(
                    Actions.sequence(
                        Actions.parallel(
                            Actions.scaleBy(3f, 3f, .25f),
                            Actions.fadeOut(.25f, Interpolation.exp5)
                        ),
                        Actions.run { engine.removeAndResetEntity(this@apply) }
                    )
                )
            }
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }
        engine.addEntity(this)
    }!!
}