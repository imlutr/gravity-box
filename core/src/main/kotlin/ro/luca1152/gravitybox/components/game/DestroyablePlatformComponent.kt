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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.engine
import ro.luca1152.gravitybox.utils.assets.Assets
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Indicates that the entity is a destroyable platform. */
class DestroyablePlatformComponent : Component, Poolable {
    var remove = false
    var isRemoved = false

    override fun reset() {
        remove = false
        isRemoved = false
    }

    fun updateScene2D(
        scene2D: Scene2DComponent,
        manager: AssetManager = Injekt.get()
    ) {
        val oldWidth = scene2D.width
        val oldCenterY = scene2D.centerY
        val oldCenterX = scene2D.centerX
        val oldRotation = scene2D.rotation
        var appendedHeight = false
        scene2D.clearChildren()
        val dotsCount = (oldWidth / (16f + 5.33f).pixelsToMeters).toInt()
        for (i in 0 until dotsCount) {
            scene2D.run {
                width += 5.33f.pixelsToMeters / 2f
                addImage(
                    manager.get(Assets.tileset).findRegion("platform-dot"),
                    appendWidth = true, appendHeight = !appendedHeight
                ).run {
                    x += 5.33f.pixelsToMeters / 2f + i * (16f + 5.33f).pixelsToMeters
                }
                appendedHeight = true
                width += 5.33f.pixelsToMeters / 2f
                originX = width / 2f
            }
        }
        scene2D.run {
            width = oldWidth
            centerX = oldCenterX
            centerY = oldCenterY
            rotation = oldRotation
        }
    }

    companion object : ComponentResolver<DestroyablePlatformComponent>(DestroyablePlatformComponent::class.java)
}

fun Entity.destroyablePlatform() =
    add(engine.createComponent(DestroyablePlatformComponent::class.java))!!

val Entity.destroyablePlatform: DestroyablePlatformComponent
    get() = DestroyablePlatformComponent[this]
