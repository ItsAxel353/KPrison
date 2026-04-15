package org.axeldev.kPrison.managers

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.axeldev.kPrison.core.Mine
import org.axeldev.kPrison.database.DatabaseManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Location

class MineManager(private val databaseManager: DatabaseManager) {

    private val mines = mutableMapOf<String, Mine>()

    init {
        loadMines()
    }

    fun addMine(mine: Mine) {
        mines[mine.id] = mine
        databaseManager.saveMine(mine)
    }

    fun getMine(id: String): Mine? {
        return mines[id]
    }

    fun getAllMines(): List<Mine> {
        return mines.values.toList()
    }

    fun removeMine(id: String) {
        mines.remove(id)
        databaseManager.deleteMine(id)
    }

    fun resetMinesIfDue() {
        for (mine in mines.values) {
            if (mine.isResetDue()) {
                mine.reset()
                regenerateMine(mine)
            }
        }
    }

    private fun regenerateMine(mine: Mine) {
        val world = mine.teleportLocation.world ?: return
        for (x in mine.minX..mine.maxX) {
            for (y in mine.minY..mine.maxY) {
                for (z in mine.minZ..mine.maxZ) {
                    val material = mine.getRandomMaterial() ?: Material.STONE
                    world.getBlockAt(x, y, z).type = material
                }
            }
        }
    }

    fun resetMine(mineId: String): Boolean {
        val mine = mines[mineId] ?: return false
        mine.reset()
        regenerateMine(mine)
        databaseManager.saveMine(mine)

        val message =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&6La mine &e${mine.id} &6a été réinitialisée !")
        Bukkit.broadcast(message)

        return true
    }

    fun setMineSpawn(mineId: String, location: Location): Boolean {
        val mine = mines[mineId] ?: return false
        val updatedMine = mine.copy(teleportLocation = location)
        mines[mineId] = updatedMine
        databaseManager.saveMine(updatedMine)
        return true
    }

    private fun loadMines() {
        mines.clear()
        val loadedMines = databaseManager.loadAllMines()
        for (mine in loadedMines) {
            mines[mine.id] = mine
        }
        println("[KPrison] ${mines.size} mines chargées depuis la base de données")
    }
}