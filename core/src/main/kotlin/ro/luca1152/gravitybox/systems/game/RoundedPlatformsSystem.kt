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
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.events.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.hitAll
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Sets the according texture to platforms so they are correctly rounded. */
class RoundedPlatformsSystem(private val context: Context) :
    EventSystem<UpdateRoundedPlatformsEvent>(context.inject(), UpdateRoundedPlatformsEvent::class) {
    // Injected objects
    private val manager: AssetManager = context.inject()

    // Entities
    private lateinit var mapEntity: Entity

    override fun addedToEngine(engine: Engine) {
        mapEntity = engine.getSingleton<MapComponent>()
    }

    override fun processEvent(event: UpdateRoundedPlatformsEvent, deltaTime: Float) {
        engine.getEntitiesFor(
            Family.one(
                PlatformComponent::class.java,
                DestroyablePlatformComponent::class.java
            ).all(Scene2DComponent::class.java).get()
        ).forEach {
            if (it.tryGet(MovingObjectComponent) == null) {
                roundCorners(it)
            } else {
                setCorrectTexture(it, 0)
            }
        }
    }

    private fun roundCorners(entity: Entity) {
        val bitmask = getIntBitmask(entity)
        setCorrectTexture(entity, bitmask)
    }

    private fun getIntBitmask(entity: Entity): Int {
        entity.scene2D.group.run {
            val isStraightBottomRightCorner = isPlatformInBottomRightOf(this) || isPlatformAtRightOf(this)
            val isStraightTopRightCorner = isPlatformInTopRightOf(this) || isPlatformAtRightOf(this)
            val isStraightTopLeftCorner = isPlatformInTopLeftOf(this) || isPlatformAtLeftOf(this)
            val isStraightBottomLeftCorner = isPlatformInBottomLeftOf(this) || isPlatformAtLeftOf(this)

            val bitmask = booleanArrayOf(
                isStraightBottomRightCorner,
                isStraightTopRightCorner,
                isStraightTopLeftCorner,
                isStraightBottomLeftCorner
            )

            return bitmaskToInt(bitmask)
        }
    }

    private fun bitmaskToInt(bitmask: BooleanArray): Int {
        require(bitmask.size <= 32) { "The bitmask is too big to be packed in an Int." }

        // Algorithm for packing a bitmask into an int
        var intValue = 0
        for (i in 0 until bitmask.size) {
            val bit = if (bitmask[i]) 1 else 0
            if (bit != 0) {
                intValue = intValue or (1 shl i)
            }
        }
        return intValue
    }

    private fun setCorrectTexture(entity: Entity, bitmask: Int) {
        entity.scene2D.run {
            // clearChildren() may affect these values
            val oldWidth = width
            val oldHeight = height
            val oldCenterX = centerX
            val oldCenterY = centerY
            val oldRotation = rotation

            clearChildren()

            val regionName = "${(if (entity.tryGet(DestroyablePlatformComponent) != null) "destroyable-" else "")}platform-$bitmask"

            addNinePatch(
                context, NinePatch(
                    manager.get(Assets.tileset).findRegion(regionName),
                    PlatformEntity.PATCH_LEFT, PlatformEntity.PATCH_RIGHT,
                    PlatformEntity.PATCH_TOP, PlatformEntity.PATCH_BOTTOM
                ), oldCenterX, oldCenterY, oldWidth, oldHeight, oldRotation
            )
        }
    }

    private fun isPlatformInBottomRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width - PlatformEntity.HEIGHT / 2f, (-5).pixelsToMeters))

    private fun isPlatformAtRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width + 5.pixelsToMeters, actor.height / 2f))

    private fun isPlatformInTopRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width - PlatformEntity.HEIGHT / 2f, actor.height + 5.pixelsToMeters))

    private fun isPlatformInTopLeftOf(actor: Actor) =
        isPlatform(actor.hitAll(PlatformEntity.HEIGHT / 2f, actor.height + 5.pixelsToMeters))

    private fun isPlatformAtLeftOf(actor: Actor) =
        isPlatform(actor.hitAll((-5).pixelsToMeters, actor.height / 2f))

    private fun isPlatformInBottomLeftOf(actor: Actor) =
        isPlatform(actor.hitAll(PlatformEntity.HEIGHT / 2f, (-5).pixelsToMeters))

    private fun isPlatform(actors: Array<Actor>): Boolean {
        actors.forEach { if (isPlatform(it)) return true }
        return false
    }

    private fun isPlatform(actor: Actor?): Boolean {
        return if (actor == null || actor.userObject == null || actor.userObject !is Entity) false
        else ((actor.userObject as Entity).tryGet(PlatformComponent) != null ||
                (actor.userObject as Entity).tryGet(DestroyablePlatformComponent) != null) &&
                (actor.userObject as Entity).tryGet(MovingObjectComponent) == null &&
                !isExtendedBounds(actor) && !isDeleted(actor.userObject as Entity)
    }

    private fun isExtendedBounds(actor: Actor?) = actor?.color == Color.CLEAR

    private fun isDeleted(entity: Entity) =
        entity.tryGet(EditorObjectComponent) != null && entity.editorObject.isDeleted
}