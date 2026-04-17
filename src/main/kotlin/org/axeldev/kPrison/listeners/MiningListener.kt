package org.axeldev.kPrison.listeners

import org.axeldev.kPrison.managers.MineManager
import org.axeldev.kPrison.managers.PrisonerManager
import org.axeldev.kPrison.managers.RankManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MiningListener(
    private val mineManager: MineManager,
    private val prisonerManager: PrisonerManager,
    private val rankManager: RankManager
) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        val prisoner = prisonerManager.getPrisoner(player.uniqueId)

        // Trouver la mine où le joueur se trouve en vérifiant les coordonnées
        val mine = mineManager.getAllMines().find { mine ->
            block.world == mine.teleportLocation.world &&
                    block.x >= mine.minX && block.x <= mine.maxX &&
                    block.y >= mine.minY && block.y <= mine.maxY &&
                    block.z >= mine.minZ && block.z <= mine.maxZ
        } ?: return

        if (!rankManager.canPrisonerAccessMine(prisoner, mine.requiredRank)) {
            player.sendMessage("§cVous n'avez pas le rang requis pour miner ici.")
            event.isCancelled = true
            return
        }
    }
}