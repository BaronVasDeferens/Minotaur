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
            println("$col $row open: $openDoors closed: ${closedDoors()}")
        }
    }

    val maze: Array<Array<Room>>

    private fun getRoom(col: Int, row: Int): Room? {
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

//        val initialRoom = maze[1][1] // getRoom(rando.nextInt(rows), rando.nextInt(columns))!!
//        val adjacentToInitialRoom = getRoomsAdjacentTo(initialRoom).random()
//        openDoors(initialRoom.col, initialRoom.row, listOf(adjacentToInitialRoom.second))
//
//        reachableRooms.add(initialRoom)
//        reachableRooms.add(adjacentToInitialRoom.first)
//
//        frontier.addAll(
//            getRoomsAdjacentTo(initialRoom)
//                .plus(getRoomsAdjacentTo(adjacentToInitialRoom.first))
//                .map { it.first }
//                .filterNot { it.isClosed() })

//        while (reachableRooms.size < rows * columns) {
//            val room = frontier.random()
//            frontier.remove(room)
//            val adjacentsToRoom = getRoomsAdjacentTo(room)
//            val reachableNeighbor = adjacentsToRoom.random()
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

        getRoom(room.col , room.row + 1)?.let {
            adjacents.add(Pair(it, SOUTH))
        }

        getRoom(room.col, room.row - 1)?.let {
            adjacents.add(Pair(it, NORTH))
        }

        getRoom(room.col + 1, room.row)?.let {
            adjacents.add(Pair(it, EAST))
        }

        getRoom(room.col - 1, room.row)?.let {
            adjacents.add(Pair(it, WEST))
        }

        return adjacents
    }

    fun openDoors(col: Int, row: Int, openTheseDoors: List<Direction>) {

        maze[col][row].open(openTheseDoors)

        // Open the corresponding door on the other side
        openTheseDoors.forEach {
            when (it) {

                NORTH -> {
                    getRoom(col, row - 1)?.open(listOf(SOUTH))
                }

                SOUTH -> {
                    getRoom(col, row + 1)?.open(listOf(NORTH))
                }

                WEST -> {
                    getRoom(col -1 , row)?.open(listOf(EAST))
                }

                EAST -> {
                    getRoom(col + 1, row)?.open(listOf(WEST))
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

        for (col in 0 until columns) {
            for (row in 0 until rows) {

                val startX = col * blockSize
                val startY = row * blockSize

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