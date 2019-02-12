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

package ro.luca1152.gravitybox.listeners

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import ktx.app.KtxInputAdapter
import ktx.math.times
import ro.luca1152.gravitybox.components.BulletComponent
import ro.luca1152.gravitybox.components.physics
import ro.luca1152.gravitybox.entities.EntityFactory
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.kotlin.screenToWorldCoordinates
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Triggers actions for every input event handled. */
class GameInputListener(private val playerEntity: Entity,
                        private val gameEventSignal: Signal<GameEvent> = Injekt.get()) : KtxInputAdapter {
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val coords = screenToWorldCoordinates(screenX, screenY)
        createBullet(coords.x, coords.y)

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.R) {
            gameEventSignal.dispatch(GameEvent.LEVEL_RESTART)
            return true
        }
        return false
    }

    private var velocity = Vector2()
    private fun createBullet(touchX: Float, touchY: Float) {
        val bullet = EntityFactory.createBullet(playerEntity)
        velocity = playerEntity.physics.body.worldCenter.cpy()
        velocity.x -= touchX; velocity.y -= touchY
        velocity.nor()
        velocity *= -BulletComponent.SPEED
        bullet.physics.body.linearVelocity = velocity
    }
}