package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component

class BulletComponent : Component {
    companion object : ComponentResolver<BulletComponent>(BulletComponent::class.java)
}