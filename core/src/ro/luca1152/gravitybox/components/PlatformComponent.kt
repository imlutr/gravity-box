package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component

class PlatformComponent : Component {
    companion object : ComponentResolver<PlatformComponent>(PlatformComponent::class.java)
}