import Room.Companion.random
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.abs

abstract class Entity {

    companion object {
        fun loadImage(fileName: String): BufferedImage {
            val resource = javaClass.classLoader.getResource(fileName)
            return ImageIO.read(resource)
        }
    }

    abstract var x: Int
    abstract var y: Int

    abstract fun getCurrentFrame(): BufferedImage
}

interface ReactiveToPLayer {
    fun reactToPlayer(player: Player)
}

interface Collectible {
    fun checkForCollection(player: Player, room: Room)
}

class Player : Entity() {

    override var x = 100
    override var y = 100

    val stones = mutableListOf<Stone>(Stone(), Stone(), Stone())

    private val imageFrames: Array<BufferedImage> = arrayOf(
        MinotaurMain.loadImage("player_still.png"),
        MinotaurMain.loadImage("player_walk_1.png"),
        MinotaurMain.loadImage("player_still.png"),
        MinotaurMain.loadImage("player_walk_2.png")
    )

    private var currentFrame = 0

    private val ticksPerFrame = 10
    private var currentTicks = 0

    private val movementIncrement = 2

    fun moveUp() {
        y -= movementIncrement
        advanceFrame()
    }

    fun moveDown() {
        y += movementIncrement
        advanceFrame()
    }

    fun moveRight() {
        x += movementIncrement
        advanceFrame()
    }

    fun moveLeft() {
        x -= movementIncrement
        advanceFrame()
    }

    private fun advanceFrame() {
        currentTicks++
        if (currentTicks >= ticksPerFrame) {
            currentFrame = (currentFrame + 1) % imageFrames.size
            currentTicks = 0
        }
    }

    override fun getCurrentFrame(): BufferedImage {
        return imageFrames[currentFrame]
    }
}

class StaticEntity(override var x: Int = random.nextInt(100, 700), override var y: Int = random.nextInt(100, 700), frameName: String) : Entity() {

    val image = loadImage(frameName)

    override fun getCurrentFrame(): BufferedImage {
       return image
    }
}

class Wraith(override var x: Int = 300, override var y: Int = 300) : Entity() {

    val imageFrames: Array<BufferedImage> = arrayOf(MinotaurMain.loadImage("wraith.png"))

    override fun getCurrentFrame(): BufferedImage {
        return imageFrames[0]
    }
}

class Rat(override var x: Int = random.nextInt(100, 700), override var y: Int = random.nextInt(100, 700)) : Entity(),
    ReactiveToPLayer {

    val imageFrames = listOf(loadImage("rat_walk_1.png"), loadImage("rat_walk_2.png"))

    private val movementIncrement = 5
    private var currentFrame = 0 + random.nextInt(2)
    private val ticksPerFrame = 10 + random.nextInt(7)
    private var currentTicks = 0


    override fun getCurrentFrame(): BufferedImage {
        return imageFrames[currentFrame]
    }

    override fun reactToPlayer(player: Player) {

        advanceFrame()

        if (currentTicks > 0) return

        if (player.x >= x) {
            x += movementIncrement
        } else {
            x -= movementIncrement
        }

        if (player.y >= y) {
            y += movementIncrement
        } else {
            y -= movementIncrement
        }


    }

    private fun advanceFrame() {
        currentTicks++
        if (currentTicks >= ticksPerFrame) {
            currentFrame = (currentFrame + 1) % imageFrames.size
            currentTicks = 0
        }
    }

}

class Stone : Entity(), Collectible {

    val image = loadImage("stone.png")
    override var x: Int = 0
    override var y: Int = 0

    override fun getCurrentFrame(): BufferedImage {
        return image
    }


    // TODO: make boolean
    override fun checkForCollection(player: Player, room: Room) {
        if (abs(player.x - x) < 10 && abs(player.y - y) < 10) {
//            player.stones.add(Stone())
//            room.removeEntity(this)
        }
    }


//    if (player.stones.size > 0) {
//        val stone = player.stones.removeAt(0)
//        stone.x = player.x
//        stone.y = player.y
//    }

}