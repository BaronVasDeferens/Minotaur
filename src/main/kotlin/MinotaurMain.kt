import GameStateManager.GameKeyEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess

object MinotaurMain {

    val gameWidth = 850;
    val gameHeight = 850
    val gameStateManager = GameStateManager()


    fun loadImage(fileName: String): BufferedImage {
        val resource = javaClass.classLoader.getResource(fileName   )
        return ImageIO.read(resource)
    }

    @ExperimentalCoroutinesApi
    val keyInputChannel = ConflatedBroadcastChannel<GameKeyEvent>()

    @ExperimentalCoroutinesApi
    private val frame = object : JFrame(), KeyListener {

        /**
         * The idea here is to capture keyboard input;
         * If esc is pressed, kill the program
         * Otherwise, transmit the input to the game manager for interpretation
         */

        override fun keyPressed(e: KeyEvent?) {
            if (e?.extendedKeyCode == KeyEvent.VK_ESCAPE) {
                exitProcess(0)
            } else {
                keyInputChannel.offer(GameKeyEvent(e!!.extendedKeyCode, true))
            }
        }

        override fun keyReleased(e: KeyEvent?) {
            keyInputChannel.offer(GameKeyEvent(e!!.extendedKeyCode, false))        }

        override fun keyTyped(e: KeyEvent?) {
            //keyInputChannel.offer(e!!)
        }

        init {
            addKeyListener(this)
        }
    }

    private val panel = object : JPanel(true) {

        var imageToDraw = BufferedImage(gameWidth, gameHeight, BufferedImage.TYPE_INT_ARGB)

        init {
            gameStateManager.gameStateRenderChannel.asFlow().onEach { image ->
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
        frame.preferredSize = Dimension(gameWidth, gameHeight)
        frame.pack()
        frame.isVisible = true
    }

}
