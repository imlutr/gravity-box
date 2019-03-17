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
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.physics.box2d.BodyDef
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Creates Box2D bodies from entities. */
class MapBodiesCreationSystem : EntitySystem() {
    private lateinit var levelEntity: Entity
    private val shouldUpdateMap
        get() = levelEntity.level.forceUpdateMap || (levelEntity.map.levelId != levelEntity.level.levelId)

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (!shouldUpdateMap)
            return

        if (levelEntity.level.forceUpdateMap) {
            levelEntity.level.forceUpdateMap = false
            updateMap()
        }
    }

    private fun updateMap() {
        levelEntity.map.run {
            destroyAllBodies()
            levelId = levelEntity.level.levelId
            createBox2DBodies()
        }
    }

    private fun createBox2DBodies() {
        engine.getEntitiesFor(Family.all(MapObjectComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                var bodyType = BodyDef.BodyType.StaticBody
                var categoryBits = EntityCategory.NONE.bits
                var maskBits = EntityCategory.NONE.bits
                var density = 1f
                var friction = .2f
                when {
                    it.tryGet(PlatformComponent) != null -> {
                        bodyType = BodyDef.BodyType.StaticBody
                        categoryBits = PlatformEntity.CATEGORY_BITS
                        maskBits = PlatformEntity.MASK_BITS
                    }
                    it.tryGet(PlayerComponent) != null -> {
                        bodyType = BodyDef.BodyType.DynamicBody
                        categoryBits = PlayerEntity.CATEGORY_BITS
                        maskBits = PlayerEntity.MASK_BITS
                        friction = PlayerEntity.FRICTION
                        density = PlayerEntity.DENSITY
                    }
                    it.tryGet(FinishComponent) != null -> {
                        bodyType = BodyDef.BodyType.StaticBody
                        categoryBits = FinishEntity.CATEGORY_BITS
                        maskBits = FinishEntity.MASK_BITS
                    }
                }
                it.body.set(
                    it.image.imageToBox2DBody(bodyType, categoryBits, maskBits, density, friction),
                    it, categoryBits, maskBits, density, friction
                )
            }
        }
    }
}