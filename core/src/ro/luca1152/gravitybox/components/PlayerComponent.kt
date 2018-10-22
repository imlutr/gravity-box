package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component

/**
 * Indicates that the entity is a player.
 */
class PlayerComponent : Component {
    companion object : ComponentResolver<PlatformComponent>(PlatformComponent::class.java)
}