package org.axeldev.kPrison.items

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MineStickListener: Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta

        if (meta?.displayName == "§6§lMine Stick") {
            event.isCancelled = true
        }
    }
}