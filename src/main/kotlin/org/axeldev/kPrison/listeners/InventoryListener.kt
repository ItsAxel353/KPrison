package org.axeldev.kPrison.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryListener : Listener {

    @EventHandler
    fun onIventoryClick(event: InventoryClickEvent) {
        if (event.view.title != "§cAmélioration de la pioche") return
        event.isCancelled = true

        val player = event.whoClicked
        val itemInHand = player.inventory.itemInMainHand

    }
}