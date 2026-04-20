package org.axeldev.kPrison.managers

import org.axeldev.kPrison.core.Rank
import org.axeldev.kPrison.core.Prisoner
import org.bukkit.entity.Player

class RankManager(private val economyManager: EconomyManager? = null) {

    private val ranks = mutableListOf<Rank>()

    init {
        loadDefaultRanks()
    }

    private fun loadDefaultRanks() {
        ranks.add(
            Rank(
                "A",
                1,
                0.0,
                listOf("mine.access.basic")))
        ranks.add(
            Rank(
                "B",
                2,
                1000.0,
                listOf("mine.access.basic", "mine.access.advanced")))
        ranks.add(
            Rank(
                "C",
                3,
                5000.0,
                listOf("mine.access.basic", "mine.access.advanced", "mine.access.premium")))
        ranks.add(
            Rank(
                "D",
                4,
                15000.0,
                listOf("mine.access.basic", "mine.access.advanced", "mine.access.premium", "mine.access.vip")
            )
        )
        ranks.sortBy { it.level }
    }

    fun getRankByName(name: String): Rank? {
        return ranks.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getRankByLevel(level: Int): Rank? {
        return ranks.find { it.level == level }
    }

    fun getAllRanks(): List<Rank> {
        return ranks.toList()
    }

    fun getNextRank(currentRank: Rank): Rank? {
        val currentIndex = ranks.indexOf(currentRank)
        return if (currentIndex >= 0 && currentIndex < ranks.size - 1) ranks[currentIndex + 1] else null
    }

    fun canPrisonerAccessMine(prisoner: Prisoner, requiredRankName: String): Boolean {
        val prisonerRank = getRankByName(prisoner.Rank) ?: return false
        val requiredRank = getRankByName(requiredRankName) ?: return false
        return prisonerRank.level >= requiredRank.level
    }

    fun promotePrisoner(prisoner: Prisoner, player: Player): Boolean {
        val currentRank = getRankByName(prisoner.Rank) ?: return false
        val nextRank = getNextRank(currentRank) ?: return false

        if (economyManager?.hasBalance(player, nextRank.requiredBalance) == true) {
            economyManager.removeBalance(player, nextRank.requiredBalance)
            prisoner.Rank = nextRank.name
            return true
        }
        return false
    }

    fun addRank(rank: Rank) {
        ranks.add(rank)
        ranks.sortBy { it.level }
    }

    fun removeRank(name: String) {
        ranks.removeIf { it.name.equals(name, ignoreCase = true) }
    }
}