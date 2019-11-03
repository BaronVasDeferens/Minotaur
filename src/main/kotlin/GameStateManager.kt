import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class GameStateManager {

    data class GameKeyEvent(val keyEvent: Int, val isPressed: Boolean)

    data class Player(var x: Int, var y: Int) {
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

        fun getCurrentFrame(): BufferedImage {
            return imageFrames[currentFrame]
        }
    }

    val player: Player
    val labyrinth = Labyrinth(8, 8)
    var currentRoom = labyrinth.getRoom(0, 0)!!

    val gameStateRenderChannel = ConflatedBroadcastChannel<BufferedImage>()

    var movingUp: Boolean = false
    var movingDown: Boolean = false
    var movingLeft: Boolean = false
    var movingRight: Boolean = false

    init {
         player = Player(200,200)

        GlobalScope.launch {

            MinotaurMain.keyInputChannel.asFlow().onEach { event ->

                // TODO: check bounds / collisions
                if (event.isPressed) {
                    when (event.keyEvent) {
                        KeyEvent.VK_D -> {
                            movingRight = true
                        }
                        KeyEvent.VK_A -> {
                            movingLeft = true
                        }
                        KeyEvent.VK_W -> {
                            movingUp = true
                        }
                        KeyEvent.VK_S -> {
                            movingDown = true
                        }
                    }
                } else {
                    when (event.keyEvent) {
                        KeyEvent.VK_D -> {
                            movingRight = false
                        }
                        KeyEvent.VK_A -> {
                            movingLeft = false
                        }
                        KeyEvent.VK_W -> {
                            movingUp = false
                        }
                        KeyEvent.VK_S -> {
                            movingDown = false
                        }
                    }
                }

            }.launchIn(GlobalScope)
        }

        // Constantly render the game state
        thread {

            while (true) {

                if (movingRight) player.moveRight()
                if (movingLeft) player.moveLeft()
                if (movingUp) player.moveUp()
                if (movingDown) player.moveDown()

                val baseImage = labyrinth.renderRoom(currentRoom)
                val g = baseImage.graphics
                g.drawImage(player.getCurrentFrame(), player.x, player.y, null)
                g.dispose()

                gameStateRenderChannel.offer(baseImage)
                sleep(10)
            }

        }



    }



}