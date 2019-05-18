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
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.CombinedPlatformEntity
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Creates Box2D bodies from entities. */
class MapBodiesCreationSystem(private val context: Context) : EntitySystem() {
    private val world: World = context.inject()

    private lateinit var levelEntity: Entity

    private val Entity.isDeleted
        get() = tryGet(EditorObjectComponent) == null && editorObject.isDeleted
    private val Entity.isCombined
        get() = tryGet(CombinedBodyComponent) != null
    private val shouldUpdateMap
        get() = levelEntity.level.forceUpdateMap || (levelEntity.map.levelId != levelEntity.level.levelId)

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
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
            updateMapBounds()
        }
    }

    private fun createBox2DBodies() {
        createCombinedPlatformsBodies()
        createOtherBodies()
    }

    private fun createCombinedPlatformsBodies() {
        separatePlatforms()
        combinePlatformsHorizontally()
        combinePlatformsVertically()
        levelEntity.level.createCombinedBodies = true
    }

    private fun separatePlatforms() {
        engine.getEntitiesFor(Family.all(CombinedBodyComponent::class.java).get()).forEach {
            it.remove(CombinedBodyComponent::class.java)
        }
    }

    private fun combinePlatformsHorizontally() {
        val platforms = engine.getEntitiesFor(Family.all(PlatformComponent::class.java).get())
        for (i in 0 until platforms.size()) {
            val entityA = platforms[i]
            if (entityA.isDeleted) {
                continue
            }
            for (j in 0 until platforms.size()) {
                val entityB = platforms[j]
                if (entityB.isDeleted || entityA == entityB) {
                    break
                }
                if (entityA.polygon.polygon.vertices.isNotEmpty() && entityB.polygon.polygon.vertices.isNotEmpty() &&
                    isSameRotation(entityA, entityB) && isHorizontalOrVertical(entityA) && isHorizontalOrVertical(entityB) &&
                    entityA.tryGet(MovingObjectComponent) == null && entityB.tryGet(MovingObjectComponent) == null &&
                    entityB.tryGet(RotatingObjectComponent) == null && entityB.tryGet(RotatingObjectComponent) == null &&
                    entityA.polygon.bottommostY == entityB.polygon.bottommostY &&
                    entityA.polygon.topmostY == entityB.polygon.topmostY &&
                    ((entityA.polygon.leftmostX == entityB.polygon.leftmostX ||
                            entityA.polygon.rightmostX == entityB.polygon.rightmostX ||
                            entityA.polygon.leftmostX == entityB.polygon.rightmostX ||
                            entityA.polygon.rightmostX == entityB.polygon.leftmostX)
                            || entityA.polygon.polygon.boundingRectangle.overlaps(entityB.polygon.polygon.boundingRectangle))
                ) {
                    combineEntities(entityA, entityB, isCombinedHorizontally = true)
                }
            }
        }
    }

    private fun combinePlatformsVertically() {
        val platforms = engine.getEntitiesFor(Family.all(PlatformComponent::class.java).get())
        for (i in 0 until platforms.size()) {
            val entityA = platforms[i]
            if (entityA.isDeleted) {
                continue
            }
            for (j in 0 until platforms.size()) {
                val entityB = platforms[j]
                if (entityB.isDeleted || entityA == entityB) {
                    break
                }
                if (entityA.polygon.polygon.vertices.isNotEmpty() && entityB.polygon.polygon.vertices.isNotEmpty() &&
                    isSameRotation(entityA, entityB) && isHorizontalOrVertical(entityA) && isHorizontalOrVertical(entityB) &&
                    entityA.tryGet(MovingObjectComponent) == null && entityB.tryGet(MovingObjectComponent) == null &&
                    entityB.tryGet(RotatingObjectComponent) == null && entityB.tryGet(RotatingObjectComponent) == null &&
                    entityA.polygon.leftmostX == entityB.polygon.leftmostX &&
                    entityA.polygon.rightmostX == entityB.polygon.rightmostX &&
                    ((entityA.polygon.bottommostY == entityB.polygon.bottommostY ||
                            entityA.polygon.topmostY == entityB.polygon.topmostY ||
                            entityA.polygon.bottommostY == entityB.polygon.topmostY ||
                            entityA.polygon.topmostY == entityB.polygon.bottommostY)
                            || entityA.polygon.polygon.boundingRectangle.overlaps(entityB.polygon.polygon.boundingRectangle))
                ) {
                    combineEntities(entityA, entityB, isCombinedVertically = true)
                }
            }
        }
    }

    private fun combineEntities(
        entityA: Entity, entityB: Entity,
        isCombinedHorizontally: Boolean = false, isCombinedVertically: Boolean = false
    ) {
        if (entityA.isCombined && !entityB.isCombined) {
            entityB.add(createComponent<CombinedBodyComponent>(context)).run {
                combinedBody.set(entityA.combinedBody.newBodyEntity!!, isCombinedHorizontally, isCombinedVertically)
            }
            entityB.body.resetInitialState()
        } else if (entityB.isCombined && !entityA.isCombined) {
            entityA.add(createComponent<CombinedBodyComponent>(context)).run {
                combinedBody.set(entityB.combinedBody.newBodyEntity!!, isCombinedHorizontally, isCombinedVertically)
            }
            entityA.body.resetInitialState()
        } else if (!entityA.isCombined && !entityB.isCombined) {
            val combinedBodyEntity =
                CombinedPlatformEntity.createEntity(context, isCombinedHorizontally, isCombinedVertically)
            entityA.add(createComponent<CombinedBodyComponent>(context)).run {
                combinedBody.set(combinedBodyEntity, isCombinedHorizontally, isCombinedVertically)
            }
            entityA.body.resetInitialState()
            entityB.add(createComponent<CombinedBodyComponent>(context)).run {
                combinedBody.set(combinedBodyEntity, isCombinedHorizontally, isCombinedVertically)
            }
            entityB.body.resetInitialState()
        } else if (entityA.isCombined && entityB.isCombined) {
            if (entityA.combinedBody.newBodyEntity != entityB.combinedBody.newBodyEntity) {
                val platformsToMerge =
                    engine.getEntitiesFor(Family.all(PlatformComponent::class.java).get()).filter {
                        it.tryGet(CombinedBodyComponent) != null && it.combinedBody.newBodyEntity == entityB.combinedBody.newBodyEntity
                    }
                val bodyEntityToRemove = platformsToMerge.first().combinedBody.newBodyEntity
                platformsToMerge.forEach {
                    if (!it.combinedBody.entityContainsBody) {
                        it.combinedBody.newBodyEntity = entityA.combinedBody.newBodyEntity
                    }
                }
                engine.removeEntity(bodyEntityToRemove!!)
            }
        }
    }

    private fun createOtherBodies() {
        engine.getEntitiesFor(Family.all(MapObjectComponent::class.java, BodyComponent::class.java).get())
            .forEach {
                if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                    var bodyType = BodyDef.BodyType.StaticBody
                    var categoryBits = EntityCategory.NONE.bits
                    var maskBits = EntityCategory.NONE.bits
                    var density = 1f
                    var friction = .2f
                    var trimSize = 0f
                    when {
                        it.tryGet(PlatformComponent) != null || it.tryGet(DestroyablePlatformComponent) != null -> {
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
                            trimSize = 0.02f
                        }
                        it.tryGet(FinishComponent) != null -> {
                            bodyType = BodyDef.BodyType.StaticBody
                            categoryBits = FinishEntity.CATEGORY_BITS
                            maskBits = FinishEntity.MASK_BITS
                        }
                    }
                    if (it.tryGet(MovingObjectComponent) != null) {
                        bodyType = BodyDef.BodyType.KinematicBody
                    }
                    if (it.tryGet(RotatingObjectComponent) != null) {
                        bodyType = BodyDef.BodyType.DynamicBody
                    }
                    if (it.tryGet(CombinedBodyComponent) == null) {
                        it.body.set(
                            context,
                            it.scene2D.toBody(context, bodyType, categoryBits, maskBits, density, friction, trimSize),
                            it, categoryBits, maskBits, density, friction
                        )
                    }
                    if (it.tryGet(RotatingObjectComponent) != null) {
                        val hookDef = BodyDef().apply {
                            position.set(it.body.body!!.worldCenter)
                        }
                        val hook = world.createBody(hookDef)
                        val jointDef = RevoluteJointDef().apply {
                            initialize(hook, it.body.body, it.body.body!!.worldCenter)
                            collideConnected = false
                            enableLimit = false
                            enableMotor = true
                            motorSpeed = -it.rotatingObject.speed
                            maxMotorTorque = 1000000f // Make the rotation not be affected by forces (such as bullets)
                        }
                        world.createJoint(jointDef)
                    }
                }
            }
    }

    private fun isSameRotation(entityA: Entity, entityB: Entity) =
        entityA.polygon.polygon.rotation == entityB.polygon.polygon.rotation || Math.abs(entityA.polygon.polygon.rotation - 180f) == entityB.polygon.polygon.rotation

    private fun isHorizontalOrVertical(entity: Entity) = entity.polygon.polygon.rotation % 90 == 0f
}