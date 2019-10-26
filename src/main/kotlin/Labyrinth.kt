import Labyrinth.Direction.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.random.Random

class Labyrinth(val columns: Int, val rows: Int, val blockSize: Int = 100) {

    val renderedImageChannel = ConflatedBroadcastChannel<BufferedImage>()

    enum class Direction {
        NORTH, EAST, SOUTH, WEST
    }

    data class Room(val col: Int, val row: Int) {
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
        else (maze[col][row])
    }

    init {


       maze = Array(columns) { colNum ->
            Array(rows) { rowNum ->
                Room(colNum, rowNum)
            }
        }

        val rando = Random

        val reachableRooms = mutableSetOf<Room>()
        val frontier = mutableSetOf<Room>()

        val initialRoom = getRoom(0,0)!! // getRoom(rando.nextInt(rows), rando.nextInt(columns))!!
//          openDoors(0,0, listOf(WEST))

//        reachableRooms.add(initialRoom)
//        frontier.addAll(getRoomsAdjacentTo(initialRoom).map { it.first })


//        while (reachableRooms.size < 8) {
//            val room = frontier.random()
//            frontier.remove(room)
//            val adjacentsToRoom = getRoomsAdjacentTo(room)
//            val reachableNeighbor = adjacentsToRoom.filterNot { it.first.isClosed() }.random()
//            openDoors(room.row, room.col, listOf(reachableNeighbor.second))
//            frontier.addAll(adjacentsToRoom.filterNot { it.first.isClosed() }.map { it.first })
//            reachableRooms.addAll(listOf<Room>(room, reachableNeighbor.first))
//        }

        renderedImageChannel.offer(renderMaze())
    }

    /*
        Returns a list of rooms adjacent to the specified room
        and the direction these room lies from the specified room
     */
    private fun getRoomsAdjacentTo(room: Room): List<Pair<Room, Direction>> {

        val adjacents = mutableListOf<Pair<Room, Direction>>()

        getRoom(room.row , room.col +1)?.let {
            adjacents.add(Pair(it, EAST))
        }

        getRoom(room.row, room.col -1)?.let {
            adjacents.add(Pair(it, WEST))
        }

        getRoom(room.row +1, room.col)?.let {
            adjacents.add(Pair(it, NORTH))
        }

        getRoom(room.row -1, room.col)?.let {
            adjacents.add(Pair(it, SOUTH))
        }

        return adjacents
    }

    fun openDoors(row: Int, col: Int, openTheseDoors: List<Direction>) {

        maze[row][col].open(openTheseDoors)

        // Open the corresponding door on the other side
        openTheseDoors.forEach {
            when (it) {

                NORTH -> {
                    getRoom(row, col - 1)?.open(listOf(SOUTH))
                }

                WEST -> {
                    getRoom(row -1 , col)?.open(listOf(EAST))
                }

                EAST -> {
                    getRoom(row + 1, col)?.open(listOf(WEST))
                }

                SOUTH -> {
                    getRoom(row, col + 1)?.open(listOf(NORTH))
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

//                getRoom(row,col)?.isClosed().let {
//                    when (it) {
//                        true ->  {
//                            g.color = Color.BLUE
//                            g.fillRect(startX, startY, blockSize, blockSize)
//                        }
//                        else -> {
//                            g.color = Color.RED
//                            g.fillRect(startX, startY, blockSize, blockSize)
//                        }
//                    }
//                }

                g.color = Color.BLACK
                maze[col][row].closedDoors().forEach { direction ->
                    when (direction) {
                        NORTH -> {
                            g.color = Color.RED
                            g.fillRect(startX, startY, blockSize, blockSize / 10)
                        }
                        SOUTH -> {
                            g.color = Color.BLUE

                            g.fillRect(startX, startY + (blockSize * .9).toInt(), blockSize, blockSize / 10)
                        }
                        WEST -> {
                            g.color = Color.YELLOW

                            g.fillRect(startX, startY , blockSize / 10, blockSize )
                        }
                        EAST -> {
                            g.color = Color.GREEN

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