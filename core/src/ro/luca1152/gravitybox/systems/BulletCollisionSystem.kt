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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import ro.luca1152.gravitybox.components.BulletComponent
import ro.luca1152.gravitybox.components.ImageComponent
import ro.luca1152.gravitybox.components.bullet
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.utils.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class BulletCollisionSystem(private val stage: GameStage = Injekt.get()) : IteratingSystem(Family.all(BulletComponent::class.java, ImageComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.bullet.collidedWithWall) {
            stage.addAction(
                    sequence(
                            delay(.01f),
                            removeActor(entity.image)
                    )
            )
            engine.removeEntity(entity)
        }
    }
}