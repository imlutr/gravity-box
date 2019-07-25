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
import com.badlogic.ashley.systems.IntervalSystem
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.NetworkComponent
import ro.luca1152.gravitybox.components.game.network
import ro.luca1152.gravitybox.utils.ads.AdsController
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable

class NetworkDetectionSystem(context: Context) : IntervalSystem(1f) {
    // Injected objects
    private val adsController: AdsController? = context.injectNullable()

    // Entities
    private lateinit var networkEntity: Entity

    override fun addedToEngine(engine: Engine) {
        networkEntity = engine.getSingleton<NetworkComponent>()
    }

    override fun updateInterval() {
        if (adsController == null) {
            return
        }

        networkEntity.network.isNetworkConnected = adsController.isNetworkConnected()
    }
}