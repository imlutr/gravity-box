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
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.editor.DeletedMapObjectComponent
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.screens.Assets
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.kotlin.hitAll
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class RoundedPlatformsSystem(
    private val manager: AssetManager = Injekt.get()
) : IteratingSystem(Family.all(PlatformComponent::class.java, ImageComponent::class.java).get()) {
    private lateinit var mapEntity: Entity

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        mapEntity = engine.getSingletonFor(Family.all(MapComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (!mapEntity.map.updateRoundedPlatforms)
            return
        super.update(deltaTime)
        mapEntity.map.updateRoundedPlatforms = false
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        roundCorners(entity)
    }

    private fun roundCorners(entity: Entity) {
        val bitmask = getIntBitmask(entity)
        setCorrectTexture(entity, bitmask)
    }

    private fun getIntBitmask(entity: Entity): Int {
        entity.image.img.run {
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
        entity.image.run {
            set(
                NinePatch(
                    manager.get(Assets.tileset).findRegion("platform-$bitmask"),
                    PlatformEntity.PATCH_LEFT,
                    PlatformEntity.PATCH_RIGHT,
                    PlatformEntity.PATCH_TOP,
                    PlatformEntity.PATCH_BOTTOM
                ), centerX, centerY, width, height, img.rotation
            )
        }
    }

    private fun isPlatformInBottomRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width - PlatformEntity.DEFAULT_THICKNESS / 2f, (-1).pixelsToMeters))

    private fun isPlatformAtRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width + 1.pixelsToMeters, actor.height / 2f))

    private fun isPlatformInTopRightOf(actor: Actor) =
        isPlatform(actor.hitAll(actor.width - PlatformEntity.DEFAULT_THICKNESS / 2f, actor.height + 1.pixelsToMeters))

    private fun isPlatformInTopLeftOf(actor: Actor) =
        isPlatform(actor.hitAll(PlatformEntity.DEFAULT_THICKNESS / 2f, actor.height + 1.pixelsToMeters))

    private fun isPlatformAtLeftOf(actor: Actor) =
        isPlatform(actor.hitAll((-1).pixelsToMeters, actor.height / 2f))

    private fun isPlatformInBottomLeftOf(actor: Actor) =
        isPlatform(actor.hitAll(PlatformEntity.DEFAULT_THICKNESS / 2f, (-1).pixelsToMeters))

    private fun isPlatform(actors: Array<Actor>): Boolean {
        actors.forEach {
            if (isPlatform(it)) {
                return true
            }
        }
        return false
    }

    private fun isPlatform(actor: Actor?): Boolean {
        return if (actor == null || actor.userObject == null || actor.userObject !is Entity) false
        else (actor.userObject as Entity).tryGet(PlatformComponent) != null && !isExtendedBounds(actor)
                && !isDeleted(actor.userObject as Entity)
    }

    private fun isExtendedBounds(actor: Actor?) = actor?.color == Color.CLEAR

    private fun isDeleted(entity: Entity) = entity.tryGet(DeletedMapObjectComponent) != null
}