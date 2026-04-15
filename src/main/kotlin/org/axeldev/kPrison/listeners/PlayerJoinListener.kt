package org.axeldev.kPrison.listeners

import org.axeldev.kPrison.managers.ScoreBoardManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerJoinListener(
    private val scoreBoardManager: ScoreBoardManager
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        scoreBoardManager.createScoreboard(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        scoreBoardManager.removeScoreboard(player)
    }
}

