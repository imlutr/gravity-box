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

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener

/**
 * @author Krustnic
 * https://github.com/krustnic/HorizontalSlidingPane
 */
class HorizontalSlidingPane(private val pageWidth: Float,
                            private val pageHeight: Float) : Group() {

    private val pages: Group = Group() // The pages container. Contains each page.

    // Offset container pages
    private var amountX = 0f

    // Offset container pages
    private var transmission = 0
    private var stopSection = 0f
    private var speed = 2500f
    private var currentSection = 1

    // Pixel speed / second after which we believe that the user wants to go to the next section
    private var flingSpeed = 600f

    private var touchFocusedChild: Actor? = null
    private val actorGestureListener: ActorGestureListener

    private val sectionsCount: Int
        get() = pages.children.size

    init {
        this.addActor(pages)

        // Input Listener
        actorGestureListener = object : ActorGestureListener() {
            override fun tap(event: InputEvent?, x: Float, y: Float, count: Int, button: Int) {}

            override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
                if (amountX - deltaX < 0f) {
                    return
                }
                if (amountX - deltaX > (pages.children.size - 1) * pageWidth) return
                amountX -= deltaX
                cancelTouchFocusedChild()
            }

            override fun fling(event: InputEvent?, velocityX: Float, velocityY: Float, button: Int) {
                if (Math.abs(velocityX) > flingSpeed) {
                    if (velocityX > 0)
                        setStopSection(currentSection - 2)
                    else
                        setStopSection(currentSection)
                }
                cancelTouchFocusedChild()
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                println("touchDown")
            }

        }
        addListener(actorGestureListener)
    }


    fun addWidget(widget: Actor) {
        pages.addActor(widget.apply {
            setPosition(pages.children.size * pageWidth - pageWidth / 2f, -pageHeight / 2f)
            setSize(pageWidth, pageHeight)
        })
    }

    /**
     * Calculation of the current section based on the displacement of the container pages.
     */
    private fun calculateCurrentSection(): Int {
        // Current section = (Current offset / length of section) + 1, because our pages are numbered from 1
        val section = Math.round(amountX / pageWidth) + 1

        // Current section = (Current offset / length of section) + 1, because our pages are numbered from 1
        if (section > pages.children.size) return pages.children.size
        return if (section < 1) 1 else section
    }

    fun setStopSection(_stoplineSection: Int) {
        var stopLineSection = _stoplineSection

        if (stopLineSection < 0) stopLineSection = 0
        if (stopLineSection > this.sectionsCount - 1) stopLineSection = this.sectionsCount - 1

        stopSection = stopLineSection * pageWidth

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
        // We shift the container with pages
        pages.x = -amountX

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
}