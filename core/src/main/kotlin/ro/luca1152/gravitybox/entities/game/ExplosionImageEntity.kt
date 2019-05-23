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
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.ColorType
import ro.luca1152.gravitybox.components.game.color
import ro.luca1152.gravitybox.components.game.explosion
import ro.luca1152.gravitybox.components.game.scene2D
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity

object ExplosionImageEntity {
    fun createEntity(
        context: Context,
        x: Float, y: Float
    ) = newEntity(context).apply {
        val manager: AssetManager = context.inject()
        val engine: PooledEngine = context.inject()

        explosion(context)
        scene2D(context, manager.get(Assets.tileset).findRegion("circle"), x, y)
        scene2D.group.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.scaleBy(3f, 3f, .25f),
                    Actions.fadeOut(.25f, Interpolation.exp5)
                ),
                Actions.run { engine.removeEntity(this@apply) }
            )
        )
        color(context, ColorType.DARK)
        addToEngine(context)
    }
}