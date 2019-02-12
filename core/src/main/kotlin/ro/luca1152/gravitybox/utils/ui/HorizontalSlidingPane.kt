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

package ro.luca1152.gravitybox.utils.ui

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener

/** A horizontal pane that contains multiple pages and can scroll between them. */
class HorizontalSlidingPane(private val pageWidth: Float,
                            private val pageHeight: Float,
                            private val flingSpeedThreshold: Float = 600f,
                            private val overScrollDistance: Float = 100f) : Group() {
    private val pages: Group = Group()

    val pageCount
        get() = pages.children.size

    /**
     * It modifies when the player pans. It is float, so it can be compared with the target page.
     * If it was int, and the player panned to page 1.5, it would get rounded to 2, and the check [currentPage != targetPage] from [scrollToTargetPage] would fail.
     */
    val currentPage
        get() = Math.abs(pages.x / pageWidth) + 1

    /** The page to which it will be automatically scrolled */
    private var targetPage = 1

    /** The x to which it will be automatically scrolled. */
    private val targetX
        get() = -((targetPage - 1) * pageWidth)

    private val inputListener = object : ActorGestureListener() {
        override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
            pages.run {
                moveBy(deltaX, 0f)
                keepXWithinBounds()
            }
        }

        override fun fling(event: InputEvent?, velocityX: Float, velocityY: Float, button: Int) {
            if (Math.abs(velocityX) > flingSpeedThreshold) {
                targetPage = when {
                    velocityX > 0 -> MathUtils.clamp(currentPage.toInt(), 1, pageCount) // The fling was to the left
                    else -> MathUtils.clamp(currentPage.toInt() + 1, 1, pageCount) // The fling was to the right
                }
            }
        }

        private fun keepXWithinBounds() {
            pages.x = MathUtils.clamp(pages.x, -((pages.children.size - 1) * pageWidth + overScrollDistance), overScrollDistance)
        }
    }

    val isPanning
        get() = inputListener.gestureDetector.isPanning

    init {
        this.addActor(pages)
        addListener(inputListener)
    }

    fun addPage(page: Actor) {
        pages.addActor(page.apply {
            // If it's not touchable, the pane slides only if you pan on the buttons, and not on the whole area.
            touchable = Touchable.enabled

            setPosition(pageCount * pageWidth - pageWidth / 2f, -pageHeight / 2f)
            setSize(pageWidth, pageHeight)
        })
    }

    override fun act(delta: Float) {
        pages.act(delta)

        if (isPanning) targetPage = Math.round(currentPage)
        else scrollToTargetPage()
    }

    private fun scrollToTargetPage() {
        if (currentPage != targetPage.toFloat() && pages.actions.count() == 0) {
            pages.addAction(moveTo(targetX, 0f, .125f))
        }
    }
}