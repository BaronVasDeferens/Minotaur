import Labyrinth.Direction.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess

object MinotaurMain {

    val labyrinth = Labyrinth(8, 8)
    var currentRoom = labyrinth.getRoom(0, 0)!!

    @ExperimentalCoroutinesApi
    val keyInputChannel = ConflatedBroadcastChannel<KeyEvent>()

    @ExperimentalCoroutinesApi
    private val frame = object : JFrame(), KeyListener {

        override fun keyPressed(e: KeyEvent?) {
            keyInputChannel.offer(e!!)
        }

        override fun keyReleased(e: KeyEvent?) {
            //keyInputChannel.offer(e!!)
        }

        override fun keyTyped(e: KeyEvent?) {
            //keyInputChannel.offer(e!!)
        }

        init {
            addKeyListener(this)
        }
    }

    private val panel = object : JPanel(true) {

        var imageToDraw = BufferedImage(850, 850, BufferedImage.TYPE_INT_ARGB)

        init {
            labyrinth.renderedImageChannel.asFlow().onEach { image ->
                imageToDraw = image
                repaint()
            }.launchIn(GlobalScope)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            g?.drawImage(imageToDraw, 0, 0, null)
            g?.dispose()
        }
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) {

        frame.layout = BorderLayout()
        frame.add(panel)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.title = "FIND THE GIRL WHILE YOU CAN"
        frame.preferredSize = Dimension(850, 850)
        frame.pack()
        frame.isVisible = true

        GlobalScope.launch {
            keyInputChannel.asFlow().onEach { keyState ->
                onKeyPressed(keyState)
            }.launchIn(GlobalScope)
        }
    }

    private fun onKeyPressed(x: KeyEvent) {
        when (x.extendedKeyCode) {

            KeyEvent.VK_W -> {
                // Move NORTH
                if (currentRoom.openDoors.contains(NORTH)) {
                    currentRoom = labyrinth.getRoom(currentRoom.col, currentRoom.row - 1)!!
                    labyrinth.renderRoom(currentRoom)
                }
            }
            KeyEvent.VK_A -> {
                // Move WEST
                if (currentRoom.openDoors.contains(WEST)) {
                    currentRoom = labyrinth.getRoom(currentRoom.col - 1, currentRoom.row)!!
                    labyrinth.renderRoom(currentRoom)
                }
            }

            KeyEvent.VK_S -> {
                // move SOUTH
                if (currentRoom.openDoors.contains(SOUTH)) {
                    currentRoom = labyrinth.getRoom(currentRoom.col, currentRoom.row + 1)!!
                    labyrinth.renderRoom(currentRoom)
                }
            }

            KeyEvent.VK_D -> {
                // Move EAST
                if (currentRoom.openDoors.contains(EAST)) {
                    currentRoom = labyrinth.getRoom(currentRoom.col + 1, currentRoom.row)!!
                    labyrinth.renderRoom(currentRoom)
                }
            }

            KeyEvent.VK_SPACE -> {
                // Initialize and render
                labyrinth.initializeMaze()
            }

            KeyEvent.VK_J -> {
                // Show the maze view
                labyrinth.publishLatestFullMazeRender()
            }

            KeyEvent.VK_K -> {
                // Show the room view
                labyrinth.renderRoom(currentRoom)
            }

            KeyEvent.VK_ESCAPE -> {
                // Quit
                exitProcess(0)
            }
        }
        println("${currentRoom.col}, ${currentRoom.row} : ${currentRoom.openDoors}")
        panel.repaint()
    }

}
