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

package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import ro.luca1152.gravitybox.PPM
import ro.luca1152.gravitybox.components.MapComponent
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object MapBodyBuilder {
    fun buildPlayerBody(tiledMap: TiledMap, world: World = Injekt.get()): Body {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
        }
        val fixtureDef = FixtureDef().apply {
            shape = MapBodyBuilder.getRectangle(tiledMap.layers.get("Player").objects[0] as RectangleMapObject)
            density = 1.15f
            friction = 2f
            filter.categoryBits = EntityCategory.PLAYER.bits
            filter.maskBits = EntityCategory.OBSTACLE.bits
        }
        return world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            fixtureDef.shape.dispose()
        }
    }

    fun buildFinishBody(tiledMap: TiledMap, world: World = Injekt.get()): Body {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
        }
        val fixtureDef = FixtureDef().apply {
            shape = MapBodyBuilder.getRectangle(tiledMap.layers.get("Finish").objects.get(0) as RectangleMapObject)
            density = 100f
            filter.categoryBits = EntityCategory.FINISH.bits
            filter.maskBits = EntityCategory.NONE.bits
        }
        return world.createBody(bodyDef).apply {
            gravityScale = 0f
            createFixture(fixtureDef)
            fixtureDef.shape.dispose()
        }
    }

    fun buildPlatforms(tiledMap: TiledMap, world: World = Injekt.get()): ArrayList<Pair<Body, MapObject>> {
        val platforms = arrayListOf<Pair<Body, MapObject>>()

        fun buildPlatformsOfType(platformType: String) {
            tiledMap.layers.get(platformType).objects.forEach { mapObject ->
                val bodyDef = BodyDef().apply {
                    type = BodyDef.BodyType.StaticBody
                }
                val platformShape = getRectangle(mapObject as RectangleMapObject)
                platforms.add(
                        Pair(world.createBody(bodyDef).apply {
                            userData = platformType == "Dynamic"
                            createFixture(platformShape, 1f)
                            platformShape.dispose()
                        }, mapObject)
                )
            }
        }

        buildPlatformsOfType("Static")
        buildPlatformsOfType("Dynamic")
        return platforms
    }

    fun buildPoints(map: MapComponent, world: World = Injekt.get()): ArrayList<Body> {
        val bodies = arrayListOf<Body>()
        map.tiledMap.layers.get("Points")?.objects?.forEach { mapObject ->
            // Increase the number points of the map
            map.totalPointsNumber++

            // Create the body
            val bodyDef = BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            }
            val fixtureDef = FixtureDef().apply {
                shape = MapBodyBuilder.getRectangle(mapObject as RectangleMapObject)
                density = 100f
                filter.categoryBits = EntityCategory.POINT.bits
                filter.maskBits = EntityCategory.NONE.bits
            }
            bodies.add(world.createBody(bodyDef).apply {
                gravityScale = 0f
                createFixture(fixtureDef)
                fixtureDef.shape.dispose()
            })
        }
        return bodies
    }

    private fun getRectangle(rectangleObject: RectangleMapObject): PolygonShape {
        val rectangle = rectangleObject.rectangle
        val size = Vector2((rectangle.x + rectangle.width * 0.5f) / PPM, (rectangle.y + rectangle.height * 0.5f) / PPM)
        return PolygonShape().apply {
            setAsBox(
                    rectangle.width * 0.5f / PPM,
                    rectangle.height * 0.5f / PPM,
                    size,
                    0.0f
            )
        }
    }
}