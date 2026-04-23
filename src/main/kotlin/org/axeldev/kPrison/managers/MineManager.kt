package org.axeldev.kPrison.managers

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.pattern.RandomPattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
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
                Bukkit.getServer().broadcastMessage("§6[KPrison] §eLe mine §6${mine.id} §ea été régénérée !")
                //recupere tout les joueurs dans la mine et les téléporte au spawn
                val teleportLocation = mine.teleportLocation
                val world = teleportLocation.world ?: continue
                for (player in world.players) {
                    if (player.location.x >= mine.minX && player.location.x <= mine.maxX &&
                        player.location.y >= mine.minY && player.location.y <= mine.maxY &&
                        player.location.z >= mine.minZ && player.location.z <= mine.maxZ) {
                        player.teleport(teleportLocation)
                    }
                }
                regenerateMineWE(mine)
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

    fun regenerateMineWE(mine: Mine) {
        val bukkitWorld = mine.teleportLocation.world ?: return

        // 1. Adapter le monde Bukkit pour WorldEdit
        val worldEditWorld = BukkitAdapter.adapt(bukkitWorld)

        // 2. Définir la région (les coordonnées de ta mine)
        val selection = CuboidRegion(
            worldEditWorld,
            BlockVector3.at(mine.minX, mine.minY, mine.minZ),
            BlockVector3.at(mine.maxX, mine.maxY, mine.maxZ)
        )

        // 3. Créer le Pattern de blocs basé sur tes pourcentages
        val pattern = RandomPattern()

        // Ici, on suppose que mine.materials est une Map<Material, Double>
        // ou une liste d'objets contenant le matériau et sa probabilité.
        mine.blocks.forEach { (material, chance) ->
            val blockData = BukkitAdapter.adapt(material.createBlockData())
            pattern.add(blockData, chance)
        }

        // 4. Exécuter l'opération via une EditSession
        WorldEdit.getInstance().newEditSession(worldEditWorld).use { editSession ->
            // On désactive l'historique (undo) pour économiser énormément de RAM sur les grosses mines
            editSession.setFastMode(true)

            // On remplit la zone d'un coup
            editSession.setBlocks(selection, pattern)
        }
    }

    fun resetMine(mineId: String): Boolean {
        val mine = mines[mineId] ?: return false
        mine.reset()
        regenerateMine(mine)
        databaseManager.saveMine(mine)
        return true
    }

    fun resetMineWE(mineId: String): Boolean {
        val mine = mines[mineId] ?: return false
        mine.reset()
        regenerateMineWE(mine)
        databaseManager.saveMine(mine)
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