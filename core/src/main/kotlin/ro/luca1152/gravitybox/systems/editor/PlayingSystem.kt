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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import ro.luca1152.gravitybox.listeners.CollisionBoxListener
import ro.luca1152.gravitybox.screens.LevelEditorScreen
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.ui.ClickButton
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayingSystem(private val levelEditorScreen: LevelEditorScreen,
                    private val uiStage: UIStage = Injekt.get(),
                    private val manager: AssetManager = Injekt.get()) : EntitySystem() {
    private val rootTable = levelEditorScreen.createRootTable()
    private lateinit var skin: Skin
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        skin = manager.get<Skin>("skins/uiskin.json")
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get()).apply {
            level.forceUpdateMap = true
        }
        hideLevelEditorUI()
        removeAllSystems(false)
        addPlaySystems()
        showPlayUI()
    }

    private fun hideLevelEditorUI() {
        levelEditorScreen.root.isVisible = false
    }

    private fun removeAllSystems(removePlayingSystem: Boolean) {
        val systemsToRemove = Array<EntitySystem>()
        engine.systems.forEach {
            if (it != this)
                systemsToRemove.add(it)
        }
        systemsToRemove.forEach {
            engine.removeSystem(it)
        }
        if (removePlayingSystem)
            engine.removeSystem(this)
    }

    private fun addPlaySystems() {
        engine.run {
            //addSystem(LevelSystem(mapEntity, finishEntity, playerEntity))
            addSystem(MapCreationSystem(levelEntity))
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem())
            addSystem(BulletCollisionSystem())
            addSystem(CollisionBoxListener())
            addSystem(PlatformRemovalSystem())
//            addSystem(PointSystem(mapEntity.map))
            addSystem(LevelOffScreenRestartSystem())
            addSystem(LevelRestartSystem())
//            addSystem(ColorSchemeSystem(mapEntity))
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(ImageRenderingSystem())
            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun showPlayUI() {
        rootTable.add(createBackButton()).expand().bottom().left()
        uiStage.addActor(rootTable)
    }

    private fun createBackButton() = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            removeAllSystems(true)
        })
    }

    override fun removedFromEngine(engine: Engine) {
        levelEditorScreen.addGameSystems()
        hidePlayUI()
        showLevelEditorUI()
        enableMoveTool()
        removePlayEntities(engine)
        resetEntitiesPosition(engine)
    }

    private fun hidePlayUI() {
        rootTable.remove()
    }

    private fun enableMoveTool() {
        levelEditorScreen.moveToolButton.isToggled = true
    }

    private fun removePlayEntities(engine: Engine) {
        removeEveryBullet(engine)
    }

    private fun resetEntitiesPosition(engine: Engine) {
        engine.getEntitiesFor(Family.all(ImageComponent::class.java, BodyComponent::class.java).get()).forEach {
            it.image.run {
                this.x = it.body.initialX
                this.y = it.body.initialY
                this.img.rotation = it.body.initialRotationRad * MathUtils.radiansToDegrees
            }
        }
    }

    private fun removeEveryBullet(engine: Engine) {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(Family.all(BulletComponent::class.java).get()).forEach {
            entitiesToRemove.add(it)
        }
        entitiesToRemove.forEach {
            engine.removeAndResetEntity(it)
        }
    }

    private fun showLevelEditorUI() {
        levelEditorScreen.root.isVisible = true
    }
}