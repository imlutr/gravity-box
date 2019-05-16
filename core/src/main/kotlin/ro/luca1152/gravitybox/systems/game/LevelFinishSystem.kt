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
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Array
import ktx.inject.Context
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.editor.ExtendedTouchComponent
import ro.luca1152.gravitybox.components.editor.MockMapObjectComponent
import ro.luca1152.gravitybox.components.editor.RotatingIndicatorComponent
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.components.game.DashedLineComponent
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors

/** Handles what happens when a level is finished. */
class LevelFinishSystem(
    context: Context,
    private val restartLevelWhenFinished: Boolean = false,
    private val playScreen: PlayScreen? = null
) : EntitySystem() {
    // Injected objects
    private val preferences: Preferences = context.inject()
    private val uiStage: UIStage = context.inject()
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    // The color scheme is the one that tells whether the level was finished: if the current color scheme
    // is the same as the dark color scheme, then it means that the level was finished. I should change
    // this in the future.
    private val colorSchemeIsFullyTransitioned
        get() = (Colors.useDarkTheme && Colors.gameColor.approxEqualTo(Colors.LightTheme.game57))
                || (!Colors.useDarkTheme && Colors.gameColor.approxEqualTo(Colors.DarkTheme.game95))
    private val levelIsFinished
        get() = playerEntity.player.isInsideFinishPoint && colorSchemeIsFullyTransitioned

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelIsFinished)
            return
        promptUserToRate()
        handleLevelFinish()
        playScreen?.shouldUpdateLevelLabel = true
    }

    private fun handleLevelFinish() {
        if (levelEntity.level.isRestarting) return

        if (restartLevelWhenFinished)
            levelEntity.level.restartLevel = true
        else {
            gameStage.addAction(
                Actions.sequence(
                    Actions.run { levelEntity.level.isRestarting = true },
                    Actions.fadeOut(0f),
                    Actions.run {
                        deleteEntities()
                        preferences.run {
                            val previousHigh = getInteger("highestFinishedLevel", 0)
                            putInteger("highestFinishedLevel", Math.max(previousHigh, levelEntity.level.levelId))
                            flush()
                        }
                        levelEntity.level.run {
                            levelId = Math.min(levelId + 1, MyGame.LEVELS_NUMBER)
                            loadMap = true
                            forceUpdateMap = true
                        }
                        levelEntity.map.run {
                            forceCenterCameraOnPlayer = true
                            resetPassengers()
                        }
                        eventQueue.add(UpdateRoundedPlatformsEvent())
                    },
                    Actions.fadeIn(.25f, Interpolation.pow3In),
                    Actions.run { levelEntity.level.isRestarting = false }
                )
            )
        }
    }

    private fun promptUserToRate() {
        if (playScreen == null) return
        if (preferences.getBoolean("neverPromptUserToRate", false)) return
        if (preferences.getInteger("promptUserToRateAfterFinishingLevel", 3) != levelEntity.level.levelId) return
        if (preferences.getBoolean("didRateGame", false)) return
        uiStage.addAction(Actions.sequence(
            Actions.delay(.25f),
            Actions.run { uiStage.addActor(playScreen.rateGamePromptPopUp) }
        ))
        preferences.run {
            val oldValue = preferences.getInteger("promptUserToRateAfterFinishingLevel", 3)
            putInteger("promptUserToRateAfterFinishingLevel", oldValue + 4)
        }
    }

    private fun deleteEntities() {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(
            Family.one(
                PlatformComponent::class.java,
                CombinedBodyComponent::class.java,
                DestroyablePlatformComponent::class.java,
                RotatingObjectComponent::class.java,
                ExplosionComponent::class.java,
                RotatingIndicatorComponent::class.java,
                DashedLineComponent::class.java,
                MockMapObjectComponent::class.java,
                TextComponent::class.java,
                BulletComponent::class.java,
                CollectiblePointComponent::class.java,
                ExtendedTouchComponent::class.java
            ).get()
        ).forEach {
            entitiesToRemove.add(it)
        }
        entitiesToRemove.forEach {
            engine.removeAndResetEntity(it)
        }
    }
}