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
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

class PromptUserToRateEvent : Event
class PromptUserToRateSystem(
    context: Context,
    private val playScreen: PlayScreen
) : EventSystem<PromptUserToRateEvent>(context.inject(), PromptUserToRateEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val uiStage: UIStage = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: PromptUserToRateEvent, deltaTime: Float) {
        promptUserToRate()
    }

    private fun promptUserToRate() {
        if (gameRules.SHOULD_SHOW_INTERSTITIAL_AD && levelEntity.level.levelId != gameRules.MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT) return
        if (gameRules.DID_RATE_THE_GAME) return
        if (gameRules.NEVER_PROMPT_USER_TO_RATE_THE_GAME) return
        if (levelEntity.level.levelId < gameRules.MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT) return
        if (gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN != 0f &&
            gameRules.PLAY_TIME < gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN
        ) return
        uiStage.addAction(
            Actions.sequence(
                Actions.delay(.25f),
                Actions.run { menuOverlayStage.addActor(playScreen.rateGamePromptPane) }
            ))
    }
}