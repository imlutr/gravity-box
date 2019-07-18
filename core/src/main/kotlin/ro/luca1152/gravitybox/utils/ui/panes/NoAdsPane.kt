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

package ro.luca1152.gravitybox.utils.ui.panes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.pay.PurchaseManager
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class NoAdsPane(context: Context, skin: Skin) : Pane(
    context, 600f,
    if (!context.inject<GameRules>().IS_AD_FREE) {
        if (context.inject<GameRules>().IS_IOS) 924f // Show the "I already paid..." button
        else 820f // Hide the "I already paid..." button
    } else 856f, skin
) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val purchaseManager: PurchaseManager? = context.injectNullable()

    val text = DistanceFieldLabel(
        context,
        (if (!gameRules.IS_AD_FREE)
            """
                Any amount below will support
                the development & REMOVE
                ADS! <3
                """
        else
            """
                The game is now ad-free!

                Any amount below will support
                the development! <3
                """).trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
    )
    val coffeeButton = Button(skin, "long-button").apply {
        val coffeeText = DistanceFieldLabel(context, "Coffee", skin, "regular", 36f, Color.WHITE)
        val priceText = DistanceFieldLabel(context, "$1.99", skin, "regular", 36f, Color.WHITE)
        add(coffeeText).padLeft(47f).expand().left()
        add(priceText).padRight(47f).expand().right()
        color.set(0 / 255f, 190 / 255f, 214 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                purchaseManager?.purchase("coffee")
                this@NoAdsPane.hide()

                // Debug
                if (!gameRules.IS_MOBILE) {
                    gameRules.IS_AD_FREE = true
                }
                return true
            }
        })
    }
    val iceCreamButton = Button(skin, "long-button").apply {
        val iceCreamText = DistanceFieldLabel(context, "Ice Cream (best)", skin, "regular", 36f, Color.WHITE)
        val priceText = DistanceFieldLabel(context, "$4.99", skin, "regular", 36f, Color.WHITE)
        add(iceCreamText).padLeft(47f).expand().left()
        add(priceText).padRight(47f).expand().right()
        color.set(207 / 255f, 0 / 255f, 214 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                purchaseManager?.purchase("ice_cream")
                this@NoAdsPane.hide()

                // Debug
                if (!gameRules.IS_MOBILE) {
                    gameRules.IS_AD_FREE = true
                }
                return true
            }
        })
    }
    val muffinButton = Button(skin, "long-button").apply {
        val muffinText = DistanceFieldLabel(context, "Muffin", skin, "regular", 36f, Color.WHITE)
        val priceText = DistanceFieldLabel(context, "$7.99", skin, "regular", 36f, Color.WHITE)
        add(muffinText).padLeft(47f).expand().left()
        add(priceText).padRight(47f).expand().right()
        color.set(24 / 255f, 178 / 255f, 230 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                purchaseManager?.purchase("muffin")
                this@NoAdsPane.hide()

                // Debug
                if (!gameRules.IS_MOBILE) {
                    gameRules.IS_AD_FREE = true
                }
                return true
            }
        })
    }
    val pizzaButton = Button(skin, "long-button").apply {
        val pizzaText = DistanceFieldLabel(context, "Pizza", skin, "regular", 36f, Color.WHITE)
        val priceText = DistanceFieldLabel(context, "$14.99", skin, "regular", 36f, Color.WHITE)
        add(pizzaText).padLeft(47f).expand().left()
        add(priceText).padRight(47f).expand().right()
        color.set(24 / 255f, 154 / 255f, 230 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                purchaseManager?.purchase("pizza")
                this@NoAdsPane.hide()

                // Debug
                if (!gameRules.IS_MOBILE) {
                    gameRules.IS_AD_FREE = true
                }
                return true
            }
        })
    }
    val sushiButton = Button(skin, "long-button").apply {
        val sushiText = DistanceFieldLabel(context, "Sushi", skin, "regular", 36f, Color.WHITE)
        val priceText = DistanceFieldLabel(context, "$24.99", skin, "regular", 36f, Color.WHITE)
        add(sushiText).padLeft(47f).expand().left()
        add(priceText).padRight(47f).expand().right()
        color.set(0 / 255f, 125 / 255f, 213 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                purchaseManager?.purchase("sushi")
                this@NoAdsPane.hide()

                // Debug
                if (!gameRules.IS_MOBILE) {
                    gameRules.IS_AD_FREE = true
                }
                return true
            }
        })
    }
    val alreadyPaidButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "I already paid...", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                purchaseManager?.purchaseRestore()
                this@NoAdsPane.hide()
            }
        })
    }
    val noThanksButton = Button(skin, "long-button").apply {
        val buttonText =
            DistanceFieldLabel(context, "No, thanks${if (!gameRules.IS_AD_FREE) " :(" else ""}", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@NoAdsPane.hide()
            }
        })
    }

    init {
        widget.run {
            add(text).padBottom(32f).row()
            add(coffeeButton).width(492f).padBottom(32f).row()
            add(iceCreamButton).width(492f).padBottom(32f).row()
            add(muffinButton).width(492f).padBottom(32f).row()
            add(pizzaButton).width(492f).padBottom(32f).row()
            add(sushiButton).width(492f).padBottom(32f).row()
            if (!gameRules.IS_AD_FREE && gameRules.IS_IOS) {
                add(alreadyPaidButton).width(492f).padBottom(32f).row()
            }
            add(noThanksButton).width(492f).row()
        }
    }
}