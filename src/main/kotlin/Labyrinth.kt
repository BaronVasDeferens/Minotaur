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
        }
    }

    val maze: Array<Array<Room>>

    fun getRoom(col: Int, row: Int): Room? {
        return if (row < 0 || row >= rows || col < 0 || col >= columns) null
        else (maze[col][row])
    }

    init {
        maze = Array(columns) { colNum ->
            Array(rows) { rowNum ->
                Room(colNum, rowNum)
            }
        }
    }

    fun initializeMaze() {
        for (a in maze) {
            for (aa in a) {
                aa.openDoors.clear()
            }
        }
        createMaze()
    }

    /*
    Using Prim's algorithm for finding a minimal spanning tree, construct a maze where every "room" is
    reachable from any other room (fully connected graph)
     */
    fun createMaze() {

        val rando = Random
        val reachableRooms = mutableSetOf<Room>()
        val frontier = mutableSetOf<Room>()

        val initialRoom = maze[rando.nextInt(columns)][rando.nextInt(rows)]

        val firstNeighbor = findAdjacentRooms(initialRoom).shuffled().first()
        openDoors(initialRoom, firstNeighbor.second)

        reachableRooms.add(initialRoom)
        reachableRooms.add(firstNeighbor.first)

        frontier.addAll(
            findAdjacentRooms(firstNeighbor.first)
                .map { it.first }
                .filter { it.isClosed() }
                .toList()
        )

        while (frontier.isNotEmpty()) {

           val room = frontier.shuffled()[0]
            frontier.remove(room)

           val adjacentRoom = findAdjacentRooms(room)
                .filter { reachableRooms.contains(it.first) }
                .toList()
                .shuffled()
                .firstOrNull()

            if (adjacentRoom != null) {
                openDoors(room, adjacentRoom.second)
                reachableRooms.add(room)

                val newFrontiers = findAdjacentRooms(room)
                    .map { it.first }
                    .filter { !reachableRooms.contains(it) }
                    .toList()

                frontier.addAll(newFrontiers)
            }
        }

        // renderedImageChannel.offer(renderMaze())
    }


    /*
        Returns a list of rooms adjacent to the specified room
        and the direction a room lies from the specified room
     */
    fun findAdjacentRooms(room: Room): List<Pair<Room, Direction>> {

        val adjacents = mutableListOf<Pair<Room, Direction>>()

        getRoom(room.col, room.row + 1)?.let {
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

    fun openDoors(room: Room, vararg openTheseDoors: Direction) {
        openDoors(room.col, room.row, openTheseDoors.asList())
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
                    getRoom(col - 1, row)?.open(listOf(EAST))
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
        g.fillRect(0, 0, 800, 800)

        g.color = Color.BLACK

        for (col in 0 until columns) {
            for (row in 0 until rows) {

                val startX = col * blockSize
                val startY = row * blockSize

                g.color = Color.BLACK
                maze[col][row].closedDoors().forEach { direction ->

                    if (maze[col][row].isClosed()) {
                        g.color = Color.GRAY
                        g.fillRect(startX, startY, blockSize, blockSize)
                    }

                    when (direction) {
                        NORTH -> {
//                            g.color = Color.RED
                            g.fillRect(startX, startY, blockSize, blockSize / 10)
                        }
                        SOUTH -> {
//                            g.color = Color.BLUE
                            g.fillRect(startX, startY + (blockSize * .9).toInt(), blockSize, blockSize / 10)
                        }
                        WEST -> {
//                            g.color = Color.YELLOW
                            g.fillRect(startX, startY, blockSize / 10, blockSize)
                        }
                        EAST -> {
//                            g.color = Color.GREEN
                            g.fillRect(startX + (blockSize * .9).toInt(), startY, blockSize / 10, blockSize)
                        }
                    }
                }
            }
        }

        g.dispose()
        return image
    }


}