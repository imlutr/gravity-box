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
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.physics.box2d.World
import ktx.actors.minus
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Handles every event related to levels, such as restarting the level.
 */
class LevelSystem(private var mapEntity: Entity,
                  private val finishEntity: Entity,
                  private val playerEntity: Entity,
                  gameEventSignal: Signal<GameEvent> = Injekt.get(),
                  private val world: World = Injekt.get(),
                  private val stage: GameStage = Injekt.get()) : EntitySystem() {
    private val eventQueue = EventQueue()

    init {
        gameEventSignal.add(eventQueue)
    }

    override fun update(deltaTime: Float) {
        eventQueue.getEvents().forEach { event ->
            if (event == GameEvent.LEVEL_RESTART) restartLevel()
        }
        if (mapEntity.map.isFinished && ColorScheme.useDarkColorScheme && ColorScheme.currentDarkColor.approxEqualTo(ColorScheme.currentDarkLerpColor))
            nextLevel()
    }

    private fun restartLevel() {
        playerEntity.player.reset(playerEntity.physics.body)
        removeBullets()
        removeExplosions()
        mapEntity.map.set(mapEntity.map.levelNumber)
    }

    private fun nextLevel() {
        fun removeAllBodies() {
            removeBullets()
            removeExplosions()
            world.bodies.forEach { body -> world.destroyBody(body) }
        }

        removeAllBodies()
        mapEntity.map.loadMap(mapEntity.map.levelNumber + 1)
        playerEntity.run {
            physics.body = MapBodyBuilder.buildPlayer(mapEntity.map.tiledMap).physics.body
            image.color = ColorScheme.currentDarkColor
        }
        finishEntity.run {
            physics.body = MapBodyBuilder.buildFinish(mapEntity.map.tiledMap).physics.body
            image.color = ColorScheme.currentDarkColor
        }
    }

    fun removeExplosions() {
        engine.getEntitiesFor(Family.all(ExplosionComponent::class.java).get()).forEach { explosion ->
            stage - explosion.image.img
            engine.removeEntity(explosion)
        }
    }

    /**
     * Removes every bullet, including their Box2D body and ImageComponent.
     */
    private fun removeBullets() {
        world.bodies.forEach { body ->
            if (body.userData is Entity && (body.userData as Entity).tryGet(BulletComponent) != null && (body.userData as Entity).tryGet(ImageComponent) != null) {
                stage - (body.userData as Entity).image.img
                world.destroyBody(body)
            }
        }
    }
}