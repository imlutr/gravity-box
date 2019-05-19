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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

class OffScreenBulletDeletionSystem(context: Context) : IteratingSystem(
    Family.all(BulletComponent::class.java, Scene2DComponent::class.java).get()
) {
    private val gameCamera: GameCamera = context.inject()
    private lateinit var levelEntity: Entity
    private val Entity.isOffScreen
        get() = scene2D.centerY < -(gameCamera.viewportHeight / 2f) + levelEntity.map.mapBottom

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.isOffScreen) {
            engine.removeEntity(entity)
        }
    }
}