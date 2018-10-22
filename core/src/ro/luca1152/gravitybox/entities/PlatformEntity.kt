package ro.luca1152.gravitybox.entities

import com.badlogic.ashley.core.Entity
import ro.luca1152.gravitybox.components.PlatformComponent

/**
 * An entity that is a platform.
 * Every Box2D body loaded from the TiledMap has the userData as [PlatformEntity].
 */
class PlatformEntity : Entity() {
    init {
        add(PlatformComponent())
    }
}