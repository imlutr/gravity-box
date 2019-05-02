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
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

class CombinedBodiesCreationSystem(private val context: Context) : EntitySystem() {
    private val world: World = context.inject()

    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.createCombinedBodies) {
            return
        }
        createBodies()
        levelEntity.level.createCombinedBodies = false
    }

    private fun createBodies() {
        createHorizontalBodies()
        createVerticalBodies()
    }

    private fun createHorizontalBodies() {
        val bodyEntities = engine.getEntitiesFor(Family.all(CombinedBodyComponent::class.java).get()).filter {
            it.combinedBody.entityContainsBody && it.combinedBody.isCombinedHorizontally
        }
        val platformsToCombine = engine.getEntitiesFor(Family.all(CombinedBodyComponent::class.java).get()).filter {
            !it.combinedBody.entityContainsBody && it.combinedBody.isCombinedHorizontally
        }
        bodyEntities.forEach { bodyEntity ->
            val platforms = platformsToCombine.filter { it.combinedBody.newBodyEntity == bodyEntity }
            combinePlatforms(bodyEntity, platforms)
        }
    }

    private fun createVerticalBodies() {
        val bodyEntities = engine.getEntitiesFor(Family.all(CombinedBodyComponent::class.java).get()).filter {
            it.combinedBody.entityContainsBody && it.combinedBody.isCombinedVertically
        }
        val platformsToCombine = engine.getEntitiesFor(Family.all(CombinedBodyComponent::class.java).get()).filter {
            !it.combinedBody.entityContainsBody && it.combinedBody.isCombinedVertically
        }
        bodyEntities.forEach { bodyEntity ->
            val platforms = platformsToCombine.filter { it.combinedBody.newBodyEntity == bodyEntity }
            combinePlatforms(bodyEntity, platforms)
        }
    }

    private fun combinePlatforms(bodyEntity: Entity, platforms: List<Entity>) {
        var leftmostX = Float.POSITIVE_INFINITY
        var rightmostX = Float.NEGATIVE_INFINITY
        var bottommostY = Float.POSITIVE_INFINITY
        var topmostY = Float.NEGATIVE_INFINITY
        platforms.forEach {
            leftmostX = Math.min(leftmostX, it.polygon.leftmostX)
            rightmostX = Math.max(rightmostX, it.polygon.rightmostX)
            bottommostY = Math.min(bottommostY, it.polygon.bottommostY)
            topmostY = Math.max(topmostY, it.polygon.topmostY)
        }
        bodyEntity.polygon.run {
            this.leftmostX = leftmostX
            this.rightmostX = rightmostX
            this.bottommostY = bottommostY
            this.topmostY = topmostY
        }
        createBodyEntityFromBounds(bodyEntity, leftmostX, rightmostX, bottommostY, topmostY)
    }

    private fun createBodyEntityFromBounds(
        bodyEntity: Entity,
        leftmostX: Float, rightmostX: Float,
        bottommostY: Float, topmostY: Float
    ) {
        val width = Math.abs(rightmostX - leftmostX)
        val height = Math.abs(topmostY - bottommostY)
        val centerX = leftmostX + width / 2f
        val centerY = bottommostY + height / 2f
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
        }
        val polygonShape = PolygonShape().apply {
            setAsBox(width / 2f, height / 2f)
        }
        val fixtureDef = FixtureDef().apply {
            shape = polygonShape
            filter.categoryBits = PlatformEntity.CATEGORY_BITS
            filter.maskBits = PlatformEntity.MASK_BITS
        }
        val body = world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            polygonShape.dispose()
            setTransform(centerX, centerY, 0f)
        }
        bodyEntity.body.set(context, body, bodyEntity, PlatformEntity.CATEGORY_BITS, PlatformEntity.MASK_BITS)
    }
}