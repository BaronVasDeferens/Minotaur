import Labyrinth.Direction.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.random.Random

class Labyrinth(
    val columns: Int,
    val rows: Int,
    val blockSize: Int = 100,
    val renderWidth: Int = 850,
    val renderHeight: Int = 850
) {

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
    private var latestFullMazeRender: BufferedImage =
        BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB)

    fun getRoom(col: Int, row: Int): Room? {
        return if (row < 0 || row >= rows || col < 0 || col >= columns) null
        else (maze[col][row])
    }

    /**
     * Returns the neighbor to the specified room.
     * If none exists, return the original room
     */
    fun getNeighbor(room: Room, direction: Direction): Room {
        when (direction) {

            NORTH -> {
                return getRoom(room.col, room.row - 1) ?: room
            }

            SOUTH -> {
                return getRoom(room.col, room.row + 1) ?: room
            }

            WEST -> {
                return getRoom(room.col - 1, room.row) ?: room
            }

            EAST -> {
                return getRoom(room.col + 1, room.row) ?: room
            }
        }
    }

    init {
        maze = Array(columns) { colNum ->
            Array(rows) { rowNum ->
                Room(colNum, rowNum)
            }
        }

        initializeMaze()
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

    @Deprecated("only counts the number of tiles in the maze; stupid")
    fun findLongestPath(room: Room, visited: MutableSet<Room>): Int {

        val unvisitedNeighbor = findAdjacentRooms(room)
            .filterNot { visited.contains(it.first) }
            .firstOrNull()

        return if (unvisitedNeighbor != null) {
            visited.add(room)
            1 + findLongestPath(unvisitedNeighbor.first, visited)
        } else {
            0
        }
    }

    fun renderMaze(): BufferedImage {

        val image = BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB)

        val g = image.graphics

        g.color = Color.WHITE
        g.fillRect(0, 0, renderWidth, renderHeight)

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
                            g.fillRect(startX, startY, blockSize, blockSize / 10)
                        }
                        SOUTH -> {
                            g.fillRect(startX, startY + (blockSize * .9).toInt(), blockSize, blockSize / 10)
                        }
                        WEST -> {
                            g.fillRect(startX, startY, blockSize / 10, blockSize)
                        }
                        EAST -> {
                            g.fillRect(startX + (blockSize * .9).toInt(), startY, blockSize / 10, blockSize)
                        }
                    }
                }
            }
        }

        g.dispose()
        latestFullMazeRender = image
        return image
    }

    fun renderRoom(room: Room): BufferedImage {

        val image = BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics

        g.color = Color.RED
        g.fillRect(0, 0, renderWidth, renderHeight)

        g.color = Color.BLACK


        val startX = room.col * blockSize
        val startY = room.row * blockSize

        val thirdWidth = (renderWidth / 3)
        val thirdHeight = (renderHeight / 3)

        // North
        g.fillRect(0, 0, thirdWidth, blockSize)
        g.fillRect(2 * thirdWidth, 0, thirdWidth, blockSize)

        // South
        g.fillRect(0, 0 + (thirdHeight * 3) - blockSize, thirdWidth, blockSize)
        g.fillRect(thirdWidth * 2, 0 + (thirdHeight * 3) - blockSize, thirdWidth, blockSize)

        // West
        g.fillRect(0, 0, blockSize, thirdHeight)
        g.fillRect(0, 2 * thirdWidth, blockSize, thirdHeight)

        // East
        g.fillRect(thirdWidth * 3 - blockSize, 0, blockSize, thirdHeight)
        g.fillRect(thirdWidth * 3 - blockSize, thirdHeight * 2, blockSize, thirdHeight)

        room.closedDoors().forEach { direction ->

            when (direction) {
                NORTH -> {
                    g.fillRect(thirdWidth, 0, thirdWidth, blockSize)
                }
                SOUTH -> {
                    g.fillRect(thirdWidth, 0 + (thirdHeight * 3) - blockSize, thirdWidth, blockSize)
                }
                WEST -> {
                    g.fillRect(0, thirdWidth, blockSize, thirdHeight)
                }
                EAST -> {
                    g.fillRect(thirdWidth * 3 - blockSize, thirdHeight, blockSize, thirdHeight)
                }
            }
        }


        g.dispose()
        //renderedImageChannel.offer(image)
        return image
    }

    fun publishLatestFullMazeRender() {
        renderedImageChannel.offer(latestFullMazeRender)
    }

}