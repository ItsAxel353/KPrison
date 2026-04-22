package org.axeldev.kPrison.items

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

class MineStickListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return

        if (item.type.isAir) return

        val displayName = meta.displayName().let {
            PlainTextComponentSerializer.plainText().serialize(it!!)
        }

        if (displayName.contains("Mine Stick")) {
            event.isCancelled = true
        }
    }

    companion object {
        private val pos1 = mutableMapOf<String, Location>()
        private val pos2 = mutableMapOf<String, Location>()

        fun setPos1(playerName: String, location: Location) {
            pos1[playerName] = location
        }

        fun setPos2(playerName: String, location: Location) {
            pos2[playerName] = location
        }

        fun getPos1(playerName: String): Location? {
            return pos1[playerName]
        }

        fun getPos2(playerName: String): Location? {
            return pos2[playerName]
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return

        val displayName = meta.displayName().let {
            PlainTextComponentSerializer.plainText().serialize(it!!)
        }

        if (displayName.contains("Mine Stick")) {
            when (event.action) {
                Action.LEFT_CLICK_BLOCK -> {
                    setPos1(player.uniqueId.toString(), event.clickedBlock!!.location)
                    player.sendMessage("§aPosition 1 définie (§f${event.clickedBlock!!.location.blockX}, ${event.clickedBlock!!.location.blockY}, ${event.clickedBlock!!.location.blockZ})")
                }
                Action.RIGHT_CLICK_BLOCK -> {
                    setPos2(player.uniqueId.toString(), event.clickedBlock!!.location)
                    player.sendMessage("§aPosition 2 définie (§f${event.clickedBlock!!.location.blockX}, ${event.clickedBlock!!.location.blockY}, ${event.clickedBlock!!.location.blockZ})")
                }
                else -> return
            }
        }
    }
}