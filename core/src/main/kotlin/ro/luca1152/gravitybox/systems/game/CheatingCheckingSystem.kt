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
import ktx.inject.Context
import pl.mk5.gdx.fireapp.GdxFIRAnalytics
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.info
import ro.luca1152.gravitybox.utils.ui.security.MyEncrypter

class CheatingCheckingSystem(context: Context) : EntitySystem() {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val myEncrypter: MyEncrypter = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        if (gameRules.IS_PLAYER_SOFT_BANNED) {
            return
        }

        if (levelEntity.map.doShotsAndEncryptedShotsDiffer()) {
            levelEntity.map.shots = myEncrypter.decrypt(levelEntity.map.encryptedShots, "encryptedShots").toInt()
            gameRules.run {
                IS_PLAYER_SOFT_BANNED = true
                flushUpdates()
            }
            info("Soft banned user. Cause: cheating by editing memory.")
            if (gameRules.IS_MOBILE) {
                GdxFIRAnalytics.inst().logEvent("soft_ban", mapOf(Pair("edited_memory", "true")))
            }
        }
    }
}