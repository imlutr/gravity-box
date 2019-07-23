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

import ktx.inject.Context
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.ui.security.SecurePreferences
import kotlin.concurrent.thread

class FlushPreferencesEvent : Event

class FlushPreferencesSystem(context: Context) : EventSystem<FlushPreferencesEvent>(context.inject(), FlushPreferencesEvent::class) {
    // Injected objects
    private val preferences: SecurePreferences = context.inject()

    override fun processEvent(event: FlushPreferencesEvent, deltaTime: Float) {
        thread {
            preferences.flush()
        }
    }
}