package org.axeldev.kPrison.managers

import org.axeldev.kPrison.core.Prisoner
import org.axeldev.kPrison.database.DatabaseManager
import org.bukkit.Bukkit
import java.util.UUID

class PrisonerManager(private val databaseManager: DatabaseManager) {

    private val prisoners = mutableMapOf<UUID, Prisoner>()

    fun getPrisoner(uuid: UUID): Prisoner {
        return prisoners.getOrPut(uuid) { 
            // Charger depuis la base de données ou créer un nouveau
            databaseManager.loadPrisoner(uuid.toString()) ?: run {
                // Créer un nouveau prisonnier avec le nom du joueur
                val player = Bukkit.getPlayer(uuid)
                val playerName = player?.name ?: "Joueur"
                Prisoner(uuid, playerName)
            }
        }
    }

    fun savePrisoner(prisoner: Prisoner) {
        prisoners[prisoner.uuid] = prisoner
        databaseManager.savePrisoner(prisoner)
    }

    fun getAllPrisoners(): List<Prisoner> {
        return prisoners.values.toList()
    }

    fun removePrisoner(uuid: UUID) {
        prisoners.remove(uuid)
    }
}
