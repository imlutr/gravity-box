package ro.luca1152.gravitybox.events

import com.badlogic.ashley.signals.Listener
import com.badlogic.ashley.signals.Signal
import java.util.*

class EventQueue : Listener<GameEvent> {
    private val eventQueue = PriorityQueue<GameEvent>()

    fun getEvents(): Array<GameEvent> {
        val events = eventQueue.toTypedArray()
        eventQueue.clear()
        return events
    }

    override fun receive(signal: Signal<GameEvent>?, event: GameEvent) {
        eventQueue.add(event)
    }
}