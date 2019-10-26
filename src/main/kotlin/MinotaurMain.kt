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

object MinotaurMain {

    val labyrinth = Labyrinth(8,8)

    @ExperimentalCoroutinesApi
    val keyInputChannel = ConflatedBroadcastChannel<KeyEvent>()

    @ExperimentalCoroutinesApi
    private val frame = object: JFrame(), KeyListener {

        override fun keyPressed(e: KeyEvent?) {
            keyInputChannel.offer(e!!)
        }

        override fun keyReleased(e: KeyEvent?) {
            keyInputChannel.offer(e!!)
        }

        override fun keyTyped(e: KeyEvent?) {
            keyInputChannel.offer(e!!)
        }

        init {
            addKeyListener(this)
        }
    }

    private val panel = object : JPanel(true) {

        var imageToDraw = BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)

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
        frame.preferredSize = Dimension(800, 800)
        frame.pack()
        frame.isVisible = true



        GlobalScope.launch {
            panel.repaint()
            keyInputChannel.asFlow().onEach { keyState ->
                onKeyPressed(keyState)
            }.launchIn(this)
        }

        GlobalScope.launch {
            while (true) {
                panel.imageToDraw = labyrinth.renderMaze()
            }
        }

    }

    private fun onKeyPressed(x: KeyEvent) {
        when (x.extendedKeyCode) {
            KeyEvent.VK_A -> {
                labyrinth.setOccupied(1,1)
            }
            KeyEvent.VK_S -> {
                labyrinth.setOccupied(2,2)
            }
            KeyEvent.VK_D -> {
                labyrinth.setOccupied(3,3)
            }
        }
        panel.repaint()
    }

}
