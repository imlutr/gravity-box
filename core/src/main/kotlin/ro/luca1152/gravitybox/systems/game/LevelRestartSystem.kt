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
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.FadeInEvent
import ro.luca1152.gravitybox.events.FadeOutEvent
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Handles what happens when a level is marked as to be restarted. */
class LevelRestartSystem(private val context: Context) : EntitySystem() {
    // Injected objects
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.restartLevel)
            return
        restartTheLevel()
    }

    private fun restartTheLevel() {
        levelEntity.level.restartLevel = false

        val fadeOutDuration = .25f
        val fadeInDuration = .25f

        gameStage.addAction(
            Actions.sequence(
                Actions.run {
                    levelEntity.level.isRestarting = true
                    eventQueue.add(FadeOutEvent(fadeOutDuration, Interpolation.pow3In))
                },
                Actions.delay(fadeOutDuration),
                Actions.run {
                    // Without this check, in the level editor, if the player restarted the level just before
                    // leaving the play test section, the game would crash
                    if (engine != null) {
                        levelEntity.map.resetPassengers()
                        resetBodiesToInitialState()
                        resetMovingPlatforms()
                        resetDestroyablePlatforms()
                        resetCollectiblePoints()
                        removeBullets()
                        levelEntity.map.forceCenterCameraOnPlayer = true
                    }
                },
                Actions.run { eventQueue.add(FadeInEvent(fadeInDuration, Interpolation.pow3In)) },
                Actions.delay(fadeInDuration),
                Actions.run { levelEntity.level.isRestarting = false }
            )
        )
    }

    private fun removeBullets() {
        val bulletsToRemove = ArrayList<Entity>()
        engine.getEntitiesFor(Family.all(BulletComponent::class.java).get()).forEach {
            bulletsToRemove.add(it)
        }
        bulletsToRemove.forEach {
            engine.removeEntity(it)
        }
    }

    private fun resetDestroyablePlatforms() {
        engine.getEntitiesFor(Family.all(DestroyablePlatformComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.run {
                    if (destroyablePlatform.isRemoved) {
                        destroyablePlatform.isRemoved = false
                        scene2D.isVisible = true
                        val bodyType = BodyDef.BodyType.StaticBody
                        val categoryBits = PlatformEntity.CATEGORY_BITS
                        val maskBits = PlatformEntity.MASK_BITS
                        body(context, scene2D.toBody(context, bodyType, categoryBits, maskBits), categoryBits, maskBits)
                    }
                }
            }
        }
    }

    private fun resetCollectiblePoints() {
        engine.getEntitiesFor(Family.all(CollectiblePointComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.run {
                    if (collectiblePoint.isCollected) {
                        collectiblePoint.isCollected = false
                        scene2D.isVisible = true
                    }
                }
            }
        }
        levelEntity.map.collectedPointsCount = 0
    }

    private fun resetBodiesToInitialState() {
        engine.getEntitiesFor(Family.all(BodyComponent::class.java).exclude(CombinedBodyComponent::class.java).get())
            .forEach {
                if ((it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) && it.tryGet(BodyComponent) != null
                    && it.tryGet(Scene2DComponent) != null
                ) {
                    if (it.body.body != null) {
                        it.body.resetToInitialState()
                        it.scene2D.run {
                            centerX = it.body.body!!.worldCenter.x
                            centerY = it.body.body!!.worldCenter.y
                        }
                    }
                }
            }
    }

    private fun resetMovingPlatforms() {
        engine.getEntitiesFor(Family.all(MovingObjectComponent::class.java).get()).forEach {
            it.movingObject.run {
                isMovingTowardsEndPoint = true
                justSwitchedDirection = true
                delayBeforeSwitching = 0f
                if (it.tryGet(LinkedEntityComponent) != null) {
                    moved(it, it.linkedEntity.get("mockPlatform"))
                } else {
                    update()
                }
            }
        }
    }
}