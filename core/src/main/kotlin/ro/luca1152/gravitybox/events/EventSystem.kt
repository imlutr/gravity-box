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

package ro.luca1152.gravitybox.events

import com.badlogic.ashley.core.EntitySystem
import kotlin.reflect.KClass

/** A simple [EntitySystem] that iterates over each event and calls [processEvent] for each one every time the system is updated.*/
abstract class EventSystem(
    private val eventType: KClass<out Event>,
    private val eventQueue: EventQueue
) : EntitySystem() {
    private val eventsToRemove = ArrayList<Event>()

    /** The update method called every tick. Calls [processEvent] for each event, then removes it from the [eventQueue]. */
    override fun update(deltaTime: Float) {
        eventQueue.forEach {
            if (it::class == eventType) {
                processEvent(it, deltaTime)
                eventsToRemove.add(it)
            }
        }
        eventQueue.removeAll(eventsToRemove)
        eventsToRemove.clear()
    }

    /**	This method is called on every event. Override this to implement your system's specific processing. */
    abstract fun processEvent(event: Event, deltaTime: Float)
}