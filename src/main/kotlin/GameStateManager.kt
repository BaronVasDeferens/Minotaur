import Direction.*
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


    val player: Player = Player()
    val labyrinth = Labyrinth(8, 8)
    var currentRoom = labyrinth.getRoom(0, 0)!!

    val gameStateRenderChannel = ConflatedBroadcastChannel<BufferedImage>()

    var movingUp: Boolean = false
    var movingDown: Boolean = false
    var movingLeft: Boolean = false
    var movingRight: Boolean = false

    init {

        // Add some entities
        labyrinth.maze[7][7].addEntity(Wraith(300, 300))
        labyrinth.maze[0][1].addEntity(Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat())
        labyrinth.maze[1][0].addEntity(Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat(), Rat())
        labyrinth.maze[2][0].addEntity(StaticEntity(200,200,"figure.png"))




        GlobalScope.launch {

            // Process input from keyboard
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
                        KeyEvent.VK_SPACE -> {
                            // drop stone
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
        // TODO: only render when "dirty"
        thread {

            while (true) {

                if (movingRight) player.moveRight()
                if (movingLeft) player.moveLeft()
                if (movingUp) player.moveUp()
                if (movingDown) player.moveDown()

                currentRoom = hasPlayerChangedRooms(player, currentRoom)

                currentRoom.getEntities().forEach {
                    if (it is ReactiveToPLayer) {
                        it.reactToPlayer(player)
                    }
                }


                val baseImage = labyrinth.renderRoom(currentRoom)
                val g = baseImage.graphics
                g.drawImage(player.getCurrentFrame(), player.x, player.y, null)
                g.dispose()

                gameStateRenderChannel.offer(baseImage)
                sleep(5)
            }

        }


    }


    private fun hasPlayerChangedRooms(player: Player, room: Room): Room {
        // FIXME: for now, a naive (read: no collision) traversal
        val openDoors = room.openDoors

        if (player.x < 0 && openDoors.contains(WEST)) {
            player.x = 750
            return labyrinth.getNeighbor(room, WEST)
        } else if (player.x > 850 && openDoors.contains(EAST)) {
            player.x = 50
            return labyrinth.getNeighbor(room, EAST)
        } else if (player.y < 0 && openDoors.contains(NORTH)) {
            player.y = 750
            return labyrinth.getNeighbor(room, NORTH)
        } else if (player.y > 850 && openDoors.contains(SOUTH)) {
            player.y = 50
            return labyrinth.getNeighbor(room, SOUTH)
        }

        return room
    }

}