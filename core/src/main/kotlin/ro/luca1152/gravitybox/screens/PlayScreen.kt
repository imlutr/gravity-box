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

package ro.luca1152.gravitybox.screens

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.app.KtxScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.clearScreen
import ro.luca1152.gravitybox.utils.kotlin.setScreen
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayScreen(
    manager: AssetManager = Injekt.get(),
    private val game: MyGame = Injekt.get(),
    private val engine: PooledEngine = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get(),
    private val world: World = Injekt.get(),
    private val inputMultiplexer: InputMultiplexer = Injekt.get(),
    private val uiStage: UIStage = Injekt.get()
) : KtxScreen {
    private lateinit var levelEntity: Entity
    private val skin = manager.get(Assets.uiSkin)
    private val backButton = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
        })
    }

    private val restartButton = ClickButton(skin, "small-button").apply {
        addIcon("redo-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            levelEntity.level.restartLevel = true
        })
    }

    private val bottomRow = Table().apply {
        add(backButton).expand().left()
        add(restartButton).expand().right()
    }

    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
        add(bottomRow).expand().fillX().bottom()
    }

    override fun show() {
        createUI()
        createGame()
    }

    private fun createGame() {
        setOwnBox2DContactListener()
        createGameEntities()
        addGameSystems()
        handleAllInput()
    }

    private fun setOwnBox2DContactListener() {
        world.setContactListener(WorldContactListener())
    }

    private fun createGameEntities() {
        levelEntity = LevelEntity.createEntity(LevelSelectorScreen.chosenLevel).apply {
            level.loadMap = true
            level.forceUpdateMap = true
        }
        PlayerEntity.createEntity()
        FinishEntity.createEntity()
    }

    private fun addGameSystems() {
        engine.run {
            addSystem(MapLoadingSystem())
            addSystem(PolygonSyncSystem())
            addSystem(MapBodiesCreationSystem())
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem())
            addSystem(BulletCollisionSystem())
            addSystem(PlatformRemovalSystem())
            addSystem(OffScreenLevelRestartSystem())
            addSystem(KeyboardLevelRestartSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(LevelFinishSystem())
            addSystem(LevelRestartSystem())
            addSystem(FinishPointColorSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(RoundedPlatformsSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(ImageRenderingSystem())
//            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun createUI() {
        uiStage.clear()
        uiStage.addActor(rootTable)
        inputMultiplexer.addProcessor(uiStage)
    }

    override fun render(delta: Float) {
        uiStage.act()
        clearScreen(Colors.bgColor)
        engine.update(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun hide() {
        world.setContactListener(null)
        Gdx.input.inputProcessor = null
    }
}