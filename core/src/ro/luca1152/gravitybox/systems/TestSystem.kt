package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.signals.Signal
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.GameEvent

class TestSystem(gameEventSignal: Signal<GameEvent>) : EntitySystem() {
    private val eventQueue = EventQueue()

    init {
        gameEventSignal.add(eventQueue)
    }

    override fun update(deltaTime: Float) {
        eventQueue.getEvents().forEach {
            when (it) {
                GameEvent.BULLET_PLATFORM_COLLISION -> println("EVENT HANDLING WORKS!!!")
            }
        }
    }
}