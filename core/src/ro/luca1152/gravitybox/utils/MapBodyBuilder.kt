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

/**
 * Code by daemonaka (https://gamedev.stackexchange.com/users/41604/daemonaka)
 */

package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.maps.Map
import com.badlogic.gdx.maps.objects.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Array

object MapBodyBuilder {
    private var ppt: Float = 0.toFloat()

    fun buildShapes(map: Map, pixels: Float, world: World): Array<Body> {
        ppt = pixels
        val objects = map.layers.get("Obstacles").objects

        val bodies = Array<Body>()

        for (`object` in objects) {

            if (`object` is TextureMapObject) {
                continue
            }

            val shape: Shape

            if (`object` is RectangleMapObject) {
                shape = getRectangle(`object`)
            } else if (`object` is PolygonMapObject) {
                shape = getPolygon(`object`)
            } else if (`object` is PolylineMapObject) {
                shape = getPolyline(`object`)
            } else if (`object` is CircleMapObject) {
                shape = getCircle(`object`)
            } else {
                continue
            }

            val bd = BodyDef()
            bd.type = BodyDef.BodyType.StaticBody
            val body = world.createBody(bd)
            body.createFixture(shape, 1f)

            bodies.add(body)

            shape.dispose()
        }
        return bodies
    }

    fun getRectangle(rectangleObject: RectangleMapObject): PolygonShape {
        val rectangle = rectangleObject.rectangle
        val polygon = PolygonShape()
        val size = Vector2((rectangle.x + rectangle.width * 0.5f) / ppt,
                (rectangle.y + rectangle.height * 0.5f) / ppt)
        polygon.setAsBox(rectangle.width * 0.5f / ppt,
                rectangle.height * 0.5f / ppt,
                size,
                0.0f)
        return polygon
    }

    private fun getPolygon(polygonObject: PolygonMapObject): PolygonShape {
        val polygon = PolygonShape()
        val vertices = polygonObject.polygon.transformedVertices

        val worldVertices = FloatArray(vertices.size)

        for (i in vertices.indices) {
            worldVertices[i] = vertices[i] / ppt
        }

        polygon.set(worldVertices)
        return polygon
    }

    private fun getPolyline(polylineObject: PolylineMapObject): ChainShape {
        val vertices = polylineObject.polyline.transformedVertices
        val worldVertices = arrayOfNulls<Vector2>(vertices.size / 2)

        for (i in 0 until vertices.size / 2) {
            worldVertices[i] = Vector2()
            worldVertices[i]!!.x = vertices[i * 2] / ppt
            worldVertices[i]!!.y = vertices[i * 2 + 1] / ppt
        }

        val chain = ChainShape()
        chain.createChain(worldVertices)
        return chain
    }

    private fun getCircle(circleObject: CircleMapObject): CircleShape {
        val circle = circleObject.circle
        val circleShape = CircleShape()
        circleShape.radius = circle.radius / ppt
        circleShape.position = Vector2(circle.x / ppt, circle.y / ppt)
        return circleShape
    }
}