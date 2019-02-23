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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.tryGet

/** Adds Box2D bodies to every map. It is called every time the level changes. */
class MapCreationSystem(private val levelEntity: Entity) : EntitySystem() {
    private val shouldUpdateMap
        get() = levelEntity.level.forceUpdateMap || (levelEntity.newMap.levelNumber != levelEntity.level.levelNumber)

    override fun update(deltaTime: Float) {
        if (!shouldUpdateMap)
            return

        if (levelEntity.level.forceUpdateMap)
            levelEntity.level.forceUpdateMap = false

        updateMap()
    }

    private fun updateMap() {
        levelEntity.newMap.run {
            reset()
            levelNumber = levelEntity.level.levelNumber
            createBox2DBodies()
        }
    }

    private fun createBox2DBodies() {
        val mapObjects = engine.getEntitiesFor(Family.all(TouchableBoundsComponent::class.java).get())
        mapObjects.forEach {
            when {
                it.tryGet(PlatformComponent) != null -> createBox2DBodyFromImage(it.image)
            }
        }
    }

    private fun createBox2DBodyFromImage(image: ImageComponent) {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
        }
        val polygonShape = PolygonShape().apply {
            setAsBox(image.width / 2f, image.height / 2f, Vector2(image.width / 2f, image.height / 2f), 0f)
            val polygon = Polygon().apply {
                vertices = floatArrayOf(0f, 0f, image.width, 0f, image.width, image.height, 0f, image.height)
                setPosition(image.img.x, image.img.y)
                setOrigin(image.width / 2f, image.height / 2f)
                rotate(image.img.rotation)
            }
            set(polygon.transformedVertices)
        }
        val fixtureDef = FixtureDef().apply {
            shape = polygonShape
        }
        levelEntity.newMap.world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            polygonShape.dispose()
        }
    }
}