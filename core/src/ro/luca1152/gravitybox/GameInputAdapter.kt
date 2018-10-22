package ro.luca1152.gravitybox

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import ktx.app.KtxInputAdapter
import ktx.math.times
import ro.luca1152.gravitybox.components.physics
import ro.luca1152.gravitybox.entities.BulletEntity
import ro.luca1152.gravitybox.entities.PlayerEntity
import ro.luca1152.gravitybox.utils.GameCamera
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class GameInputAdapter(private val playerEntity: PlayerEntity = Injekt.get(),
                       private val gameCamera: GameCamera = Injekt.get(),
                       private val engine: Engine = Injekt.get()) : KtxInputAdapter {
    private val worldCoordinates = Vector3()
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // Translate the screen coordinates to world coordinates
        worldCoordinates.x = screenX.toFloat(); worldCoordinates.y = screenY.toFloat()
        gameCamera.unproject(worldCoordinates)

        // Create a bullet
        createBullet(worldCoordinates.x, worldCoordinates.y)
        return true
    }

    private var velocity = Vector2()
    private fun createBullet(touchX: Float, touchY: Float) {
        val bullet = BulletEntity()
        velocity = playerEntity.physics.body.worldCenter.cpy()
        velocity.x -= touchX; velocity.y -= touchY
        velocity.nor()
        velocity *= -BulletEntity.SPEED
        bullet.physics.body.linearVelocity = velocity
        engine.addEntity(bullet)
    }
}