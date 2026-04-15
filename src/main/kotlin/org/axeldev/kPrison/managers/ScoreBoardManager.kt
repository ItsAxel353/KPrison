package org.axeldev.kPrison.managers

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

class ScoreBoardManager(
    private val prisonerManager: PrisonerManager,
    private val rankManager: RankManager
) {

    private val playerScoreboards = mutableMapOf<String, Any>()

    fun createScoreboard(player: Player) {
        val scoreboardManager = Bukkit.getScoreboardManager()!!
        val scoreboard = scoreboardManager.newScoreboard
        val objective = scoreboard.registerNewObjective("kprison", "dummy", "§6§lKPRISON")
        objective.displaySlot = DisplaySlot.SIDEBAR

        updateScoreboard(player, scoreboard)
        player.scoreboard = scoreboard
        playerScoreboards[player.uniqueId.toString()] = scoreboard
    }

    fun updateScoreboard(player: Player, scoreboard: Any) {
        val prisoner = prisonerManager.getPrisoner(player.uniqueId)

        if (scoreboard !is org.bukkit.scoreboard.Scoreboard) return

        val objective = scoreboard.getObjective("kprison") ?: return

        // Réinitialiser les scores
        for (entry in scoreboard.entries.toList()) {
            scoreboard.resetScores(entry)
        }

        // Ajouter les nouvelles lignes avec des scores décroissants
        var score = 15

        // Ligne vide
        objective.getScore("§0").score = score--

        // Balance
        objective.getScore("§7§l> §6Solde").score = score--
        objective.getScore("§e  §f${String.format("%.2f", prisoner.balance)}€").score = score--

        // Ligne vide
        objective.getScore("§1").score = score--

        // Rang
        objective.getScore("§7§l> §6Rang").score = score--
        objective.getScore("§e  §a${prisoner.Rank}").score = score--

        // Ligne vide
        objective.getScore("§2").score = score--

        // Obtenir le prochain rang
        val currentRank = rankManager.getRankByName(prisoner.Rank)
        if (currentRank != null) {
            val nextRank = rankManager.getNextRank(currentRank)
            if (nextRank != null) {
                objective.getScore("§7§l> §6Prochain").score = score--
                objective.getScore("§e  §a${nextRank.name}").score = score--

                val remaining = (nextRank.requiredBalance - prisoner.balance).toLong()
                objective.getScore("§e  §7${remaining}€ restants").score = score--

                // Barre de progression
                val progressBar = createProgressBar(prisoner.balance, nextRank.requiredBalance)
                objective.getScore("§e$progressBar").score = score--
            } else {
                objective.getScore("§7§l> §6Prochain").score = score--
                objective.getScore("§e  §cRANG MAX!").score = score
            }
        }
        objective.getScore("§7").score = score--
        objective.getScore("§eserver.ip").score = score
    }

    private fun createProgressBar(current: Double, max: Double): String {
        val percentage = (current / max * 100).toInt().coerceIn(0, 100)
        val filledBars = percentage / 5
        val emptyBars = 20 - filledBars

        val filled = "§a§l|".repeat(filledBars)
        val empty = "§8§l|".repeat(emptyBars)

        return "§7[$filled$empty§7] §f$percentage%"
    }

    fun removeScoreboard(player: Player) {
        playerScoreboards.remove(player.uniqueId.toString())
        player.scoreboard = Bukkit.getScoreboardManager()!!.mainScoreboard
    }

    fun getScoreboard(player: Player): Any? {
        return playerScoreboards[player.uniqueId.toString()]
    }

    fun updateAllScoreboards() {
        for (player in Bukkit.getOnlinePlayers()) {
            val scoreboard = getScoreboard(player)
            if (scoreboard != null) {
                updateScoreboard(player, scoreboard)
            }
        }
    }
}
