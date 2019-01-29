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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener

/**
 * @author Krustnic
 * https://github.com/krustnic/HorizontalSlidingPane
 */
class HorizontalSlidingPane : Group() {
    private val sectionWidth: Float
    private val sectionHeight: Float

    // Section container
    private val sections: Group = Group()

    // Offset container sections
    private var amountX = 0f

    // Offset container sections
    private var transmission = 0
    private var stopSection = 0f
    private var speed = 1500f
    private var currentSection = 1

    // Pixel speed / second after which we believe that the user wants to go to the next section
    private var flingSpeed = 1000f

    private var overScrollDistance = 100f

    private val cullingArea1 = Rectangle()
    private var touchFocusedChild: Actor? = null
    private val actorGestureListener: ActorGestureListener

    val sectionsCount: Int
        get() = sections.children.size

    init {
        this.addActor(sections)
        sectionWidth = Gdx.app.graphics.width.toFloat()
        sectionHeight = Gdx.app.graphics.height.toFloat()
        sections.touchable = Touchable.enabled
        actorGestureListener = object : ActorGestureListener() {
            override fun tap(event: InputEvent?, x: Float, y: Float, count: Int, button: Int) {}

            override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
                if (amountX < -overScrollDistance) {
                    return
                }
                if (amountX > (sections.children.size - 1) * sectionWidth + overScrollDistance) return
                amountX -= deltaX
                cancelTouchFocusedChild()
            }
//
//            override fun fling(event: InputEvent?, velocityX: Float, velocityY: Float, button: Int) {
//                if (Math.abs(velocityX) > flingSpeed) {
//                    if (velocityX > 0)
//                        setStopSection(currentSection - 2)
//                    else
//                        setStopSection(currentSection)
//                }
//                cancelTouchFocusedChild()
//            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                println("touchDown")
            }

        }
        addListener(actorGestureListener)
    }

    fun addWidget(widget: Actor) {
        sections.addActor(widget.apply {
            setPosition(sections.children.size * sectionWidth - sectionWidth / 2f, -sectionHeight / 2f)
            setSize(sectionWidth, sectionHeight)
        })
    }

    /**
     * Calculation of the current section based on the displacement of the container sections.
     */
    private fun calculateCurrentSection(): Int {
        // Current section = (Current offset / length of section) + 1, because our sections are numbered from 1
        val section = Math.round(amountX / sectionWidth) + 1

        // Current section = (Current offset / length of section) + 1, because our sections are numbered from 1
        if (section > sections.children.size) return sections.children.size
        return if (section < 1) 1 else section
    }

    fun setStopSection(_stoplineSection: Int) {
        var stopLineSection = _stoplineSection

        if (stopLineSection < 0) stopLineSection = 0
        if (stopLineSection > this.sectionsCount - 1) stopLineSection = this.sectionsCount - 1

        stopSection = stopLineSection * sectionWidth

        // Determine the direction of movement
        // transmission ==  1 - to the right
        // transmission == -1 - to the left
        if (amountX < stopSection) {
            transmission = 1
        } else {
            transmission = -1
        }
    }

    private fun move(delta: Float) {
        // Determine the direction of the offset
        if (amountX < stopSection) {
            // Move right
            // If you got here, but at the same time had to move to the left
            // it means it's time to stop
            if (transmission == -1) {
                amountX = stopSection

                // Fix the current section number
                currentSection = calculateCurrentSection()

                return
            }

            // Shift
            amountX += speed * delta
        } else if (amountX > stopSection) {
            if (transmission == 1) {
                amountX = stopSection
                currentSection = calculateCurrentSection()
                return
            }
            amountX -= speed * delta
        }
    }

    override fun act(delta: Float) {
        // We shift the container with sections
        sections.x = -amountX

        cullingArea1.set(-sections.x + 50, sections.y, sectionWidth - 100, sectionHeight)
        sections.cullingArea = cullingArea1

        // If we drive a finger across the screen
        if (actorGestureListener.gestureDetector.isPanning) {
            // Set the border to which we will animate the movement
            // border = number of the previous section
            setStopSection(calculateCurrentSection() - 1)
        } else {
            // If the finger is far from the screen - we animate the movement to a given point.
            move(delta)
        }
    }

    internal fun cancelTouchFocusedChild() {
        when (touchFocusedChild) {
            null -> return
            else -> stage.cancelTouchFocus(this)
        }
        touchFocusedChild = null
    }

    fun setFlingSpeed(_flingSpeed: Float) {
        flingSpeed = _flingSpeed
    }

    fun setSpeed(_speed: Float) {
        speed = _speed
    }

    fun setOverscrollDistance(_overscrollDistance: Float) {
        overScrollDistance = _overscrollDistance
    }

}