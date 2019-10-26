import java.awt.Color
import java.awt.image.BufferedImage

class Labyrinth(val rows: Int, val columns: Int, val blockSize: Int = 100) {

    data class Room(val row: Int, val col: Int) {
        var occupied = false
    }

    val maze: Array<Array<Room>>

    init {
        maze = Array(rows) { rowNum ->
            Array(columns) { colNum ->
                Room(rowNum, colNum)
            }
        }
    }

    fun setOccupied(row: Int, col: Int) {
        maze[row][col].occupied = true
    }

    fun renderMaze(): BufferedImage {

        val image = BufferedImage(800,800, BufferedImage.TYPE_INT_ARGB)

        val g = image.graphics
        g.color = Color.BLACK

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val startX = row * blockSize
                val startY = col * blockSize
                when (maze[row][col].occupied) {
                    true ->
                        g.fillRect(startX, startY, blockSize, blockSize)
                    false ->
                        g.drawRect(startX, startY, blockSize, blockSize)
                }
            }
        }

        g.dispose()
        return image
    }


}