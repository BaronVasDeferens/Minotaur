import Labyrinth.Direction.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.awt.Color
import java.awt.image.BufferedImage

class Labyrinth(val rows: Int, val columns: Int, val blockSize: Int = 100) {

    val renderedImageChannel = ConflatedBroadcastChannel<BufferedImage>()

    enum class Direction {
        NORTH, EAST, SOUTH, WEST
    }

    data class Room(val row: Int, val col: Int) {
        var openDoors = mutableSetOf<Direction>()

        fun closedDoors() = Direction.values().toMutableSet().minus(openDoors)

        fun isClosed(): Boolean {
            return openDoors.isEmpty()
        }

        fun open(openTheseDoors: List<Direction>) {
            openDoors.addAll(openTheseDoors)
            println("$row $col open: $openDoors closed: ${closedDoors()}")
        }
    }

    val maze: Array<Array<Room>>

    private fun getRoom(row: Int, col: Int): Room? {
        return if (row < 0 || row >= rows || col < 0 || col >= columns) null
        else (maze[row][col])
    }

    init {
        maze = Array(rows) { rowNum ->
            Array(columns) { colNum ->
                Room(rowNum, colNum)
            }
        }

        renderedImageChannel.offer(renderMaze())
    }

    fun openDoors(row: Int, col: Int, openTheseDoors: List<Direction>) {

        maze[row][col].open(openTheseDoors)

        openTheseDoors.forEach {
            when (it) {
                NORTH -> {
                    getRoom(row, col - 1)?.open(listOf(SOUTH))
                }

                SOUTH -> {
                    getRoom(row , col + 1)?.open(listOf(NORTH))
                }

                EAST -> {
                    getRoom(row +1  , col)?.open(listOf(WEST))
                }

                WEST -> {
                    getRoom(row - 1, col )?.open(listOf(EAST))
                }
            }
        }

        renderedImageChannel.offer(renderMaze())

    }

    fun renderMaze(): BufferedImage {

        val image = BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)

        val g = image.graphics

        g.color = Color.WHITE
        g.fillRect(0,0,800,800)

        g.color = Color.BLACK

        for (row in 0 until rows) {
            for (col in 0 until columns) {

                val startX = row * blockSize
                val startY = col * blockSize

                getRoom(row,col)?.isClosed().let {
                    when (it) {
                        true ->  {
                            g.color = Color.BLUE
                            g.fillRect(startX, startY, blockSize, blockSize)
                        }
                        else -> {
                            g.color = Color.RED
                            g.fillRect(startX, startY, blockSize, blockSize)
                        }
                    }
                }

                g.color = Color.BLACK
                maze[row][col].closedDoors().forEach { direction ->
                    when (direction) {
                        NORTH -> {
                            g.fillRect(startX, startY, blockSize, blockSize / 10)
                        }
                        SOUTH -> {
                            g.fillRect(startX, startY + (blockSize * .9).toInt(), blockSize, blockSize / 10)
                        }
                        WEST -> {
                            g.fillRect(startX, startY , blockSize / 10, blockSize )
                        }
                        EAST -> {
                            g.fillRect(startX + (blockSize * .9).toInt(), startY , blockSize / 10, blockSize )
                        }
                    }
                }
            }
        }

        g.dispose()
        return image
    }


}