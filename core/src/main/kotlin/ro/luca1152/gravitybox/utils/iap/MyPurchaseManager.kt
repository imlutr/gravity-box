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

package ro.luca1152.gravitybox.utils.iap

import com.badlogic.gdx.pay.*
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.ui.panes.*

class MyPurchaseManager(context: Context) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val purchaseManager: PurchaseManager? = context.injectNullable()
    private val menuOverlayStage: MenuOverlayStage = context.inject()

    // Panes
    private val restoreSuccessPane = RestoreSuccessPane(context)
    private val restoreNoPurchasesErrorPane = RestoreNoPurchasesErrorPane(context)
    private val restoreErrorPane = RestoreErrorPane(context)
    private val purchaseErrorPane = PurchaseErrorPane(context)
    private val purchaseSuccessPane = PurchaseSuccessPane(context)

    fun init() {
        if (!gameRules.IS_MOBILE)
            return

        val purchaseObserver = object : PurchaseObserver {
            override fun handleInstall() {
                // Restore purchases every time the game launches, thus if the game is reinstalled, the game remains ad-free (if a purchase was made)
                // Do this only on Android, as Apple forbids restoring purchases without any user interaction
                // => on iOS there will be an "I already paid..." button
                if (gameRules.IS_ANDROID) {
                    purchaseManager!!.purchaseRestore()
                }
            }

            override fun handleInstallError(e: Throwable?) {}

            override fun handleRestore(transactions: Array<out Transaction>) {
                if (transactions.isEmpty() && gameRules.IS_IOS) {
                    menuOverlayStage.addActor(restoreNoPurchasesErrorPane)
                } else {
                    transactions.forEach {
                        handleTransaction(it)
                    }

                    // Silently restore on Android
                    if (gameRules.IS_IOS) {
                        menuOverlayStage.addActor(restoreSuccessPane)
                    }
                }
            }

            override fun handleRestoreError(e: Throwable?) {
                // Silently handle restore errors on Android
                if (gameRules.IS_IOS) {
                    menuOverlayStage.addActor(restoreErrorPane)
                }
            }

            override fun handlePurchase(transaction: Transaction) {
                handleTransaction(transaction)
                menuOverlayStage.addActor(purchaseSuccessPane)
            }

            private fun handleTransaction(transaction: Transaction) {
                if (transaction.isPurchased && transaction.affectsAds()) {
                    gameRules.IS_AD_FREE = true
                }
            }

            override fun handlePurchaseError(e: Throwable?) {
                menuOverlayStage.addActor(purchaseErrorPane)
            }

            override fun handlePurchaseCanceled() {}

            private fun Transaction.affectsAds() =
                identifier == "coffee" || identifier == "ice_cream" || identifier == "muffin" || identifier == "pizza" || identifier == "sushi"
        }
        val purchaseManagerConfig = PurchaseManagerConfig().apply {
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("coffee"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("ice_cream"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("muffin"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("pizza"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("sushi"))
        }
        purchaseManager!!.install(purchaseObserver, purchaseManagerConfig, true)
    }
}