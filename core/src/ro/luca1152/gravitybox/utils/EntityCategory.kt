package ro.luca1152.gravitybox.utils

/**
 * Used for Box2D collision detection.
 * Every Box2D body has its own bits (categoryBits) and the bits of the bodies it can collide with (maskBits).
 */
enum class EntityCategory(bits: Int) {
    NONE(0x0000),
    FINISH(0x0001),
    PLAYER(0x0002),
    OBSTACLE(0x0003),
    BULLET(0x0004);

    var bits: Short = bits.toShort()
}