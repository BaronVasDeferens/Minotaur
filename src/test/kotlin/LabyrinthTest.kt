import Labyrinth.Direction.EAST
import Labyrinth.Direction.WEST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LabyrinthTest {

    @Test
    fun `should find only 2 adjacent spaces`() {
        val labyrinth = Labyrinth(3,3)
        val room = labyrinth.getRoom(0,0)!!
        val adjacentRooms = labyrinth.findAdjacentRooms(room)

        assertEquals(2, adjacentRooms.size)
    }

    @Test
    fun `should find only 3 adjacent spaces`() {
        val labyrinth = Labyrinth(3,3)
        val room = labyrinth.getRoom(1,0)!!
        val adjacentRooms = labyrinth.findAdjacentRooms(room)

        assertEquals(3, adjacentRooms.size)
    }

    @Test
    fun `should find all 4 adjacent spaces`() {
        val labyrinth = Labyrinth(3,3)
        val room = labyrinth.getRoom(1,1)!!
        val adjacentRooms = labyrinth.findAdjacentRooms(room)

        assertEquals(4, adjacentRooms.size)
    }

    @Test
    fun `should correctly identify adjacent spaces`() {
        val labyrinth = Labyrinth(3,3)
        val room = labyrinth.getRoom(1,1)!!
        val adjacentRooms = labyrinth.findAdjacentRooms(room)
            .sortedBy { it.second.name }
            .map { Pair(it.first.col, it.first.row) }

        assertEquals(adjacentRooms[0], Pair(2,1))   // EAST
        assertEquals(adjacentRooms[1], Pair(1,0))   // NORTH
        assertEquals(adjacentRooms[2], Pair(1,2))   // SOUTH
        assertEquals(adjacentRooms[3], Pair(0,1))   // WEST
    }

    @Test
    fun `should open doors`() {
        val labyrinth = Labyrinth(3,3)
        val room = labyrinth.getRoom(1,1)!!
        val neighbor = labyrinth.getRoom(2,1)!!
        labyrinth.openDoors(room, EAST)

        assertEquals(1, room.openDoors.size)
        assertTrue(room.openDoors.contains(EAST))
        assertEquals(1, neighbor.openDoors.size)
        assertTrue(neighbor.openDoors.contains(WEST))
    }
}