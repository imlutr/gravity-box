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

@file:Suppress("MemberVisibilityCanBePrivate")

package ro.luca1152.gravitybox.entities.game

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.screens.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.toMeters
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object PlatformEntity {
    const val PATCH_LEFT = 9
    const val PATCH_RIGHT = 9
    const val PATCH_TOP = 5
    const val PATCH_BOTTOM = 5
    const val DEFAULT_ROTATION = 0f
    const val DEFAULT_THICKNESS = .25f
    val CATEGORY_BITS = EntityCategory.PLATFORM.bits
    val MASK_BITS = EntityCategory.OBSTACLE.bits

    fun createEntity(
        id: Int, x: Float, y: Float,
        width: Float, height: Float = DEFAULT_THICKNESS,
        rotationInDeg: Float = DEFAULT_ROTATION,
        isDestroyable: Boolean = false,
        engine: PooledEngine = Injekt.get(),
        manager: AssetManager = Injekt.get()
    ) = engine.createEntity().apply {
        add(engine.createComponent(MapObjectComponent::class.java)).run {
            mapObject.set(id)
        }
        if (!isDestroyable) {
            add(engine.createComponent(PlatformComponent::class.java))
            add(engine.createComponent(ImageComponent::class.java)).run {
                image.set(
                    NinePatch(
                        manager.get(Assets.tileset).findRegion("platform-0"),
                        PATCH_LEFT, PATCH_RIGHT,
                        PATCH_TOP, PATCH_BOTTOM
                    ),
                    x, y, width, height, rotationInDeg
                )
                image.img.userObject = this
            }
        } else {
            add(engine.createComponent(DestroyablePlatformComponent::class.java))
            add(engine.createComponent(ImageComponent::class.java)).run {
                image.img.run {
                    setPosition(x, y)
                    setSize(width, height)
                    rotation = rotationInDeg
                }
            }
            add(engine.createComponent(GroupComponent::class.java)).run {
                group.run {
                    set(image)
                    group.run {
                        addActor(Image(manager.get(Assets.tileset).findRegion("platform-dot")).toMeters())
                        addActor(Image(manager.get(Assets.tileset).findRegion("platform-dot")).toMeters().apply {
                            this.x += this.width + 5.33f.pixelsToMeters
                        })
                        addActor(Image(manager.get(Assets.tileset).findRegion("platform-dot")).toMeters().apply {
                            this.x += 2 * this.width + 2 * 5.33f.pixelsToMeters
                        })
                        setPosition(x - 1f / 2f + 2.66f.pixelsToMeters, y - 16.pixelsToMeters / 2f)
                    }
                }
            }
        }
        add(engine.createComponent(PolygonComponent::class.java)).run {
            polygon.set(image.img)
            polygon.update()
        }
        add(engine.createComponent(EditorObjectComponent::class.java))
        add(engine.createComponent(SnapComponent::class.java))
        add(engine.createComponent(BodyComponent::class.java))
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }
        add(engine.createComponent(OverlayComponent::class.java)).run {
            overlay.set(
                showMovementButtons = true,
                showRotationButton = true,
                showResizingButtons = true,
                showDeletionButton = true
            )
        }
        add(engine.createComponent(ExtendedTouchComponent::class.java)).run {
            extendedTouch.set(this, 0f, 1f - height)
        }
        add(engine.createComponent(JsonComponent::class.java)).run {
            json.setArrayObject(this)
        }

        engine.addEntity(this)
    }!!
}