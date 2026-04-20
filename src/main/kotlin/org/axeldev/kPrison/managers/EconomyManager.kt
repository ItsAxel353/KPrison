package org.axeldev.kPrison.managers

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class EconomyManager(plugin: JavaPlugin) {

    private var economy: Economy? = null

    init {
        setupEconomy(plugin)
    }

    private fun setupEconomy(plugin: JavaPlugin): Boolean {
        if (plugin.server.pluginManager.getPlugin("Vault") == null) {
            println("[KPrison] ❌ Vault n'est pas installé. L'économie ne fonctionnera pas.")
            return false
        }

        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            println("[KPrison] ❌ Aucun plugin d'économie trouvé avec Vault.")
            return false
        }

        economy = rsp.provider
        println("[KPrison] ✅ ${economy?.name} intégré avec succès via Vault")
        return true
    }

    fun getBalance(uuid: UUID): Double {
        val player = Bukkit.getPlayer(uuid) ?: return 0.0
        return economy?.getBalance(player) ?: 0.0
    }

    fun getBalance(player: Player): Double {
        return economy?.getBalance(player) ?: 0.0
    }

    fun setBalance(uuid: UUID, amount: Double) {
        val player = Bukkit.getPlayer(uuid) ?: return
        economy?.let {
            val current = it.getBalance(player)
            if (amount > current) {
                it.depositPlayer(player, amount - current)
            } else {
                it.withdrawPlayer(player, current - amount)
            }
        }
    }

    fun setBalance(player: Player, amount: Double) {
        economy?.let {
            val current = it.getBalance(player)
            if (amount > current) {
                it.depositPlayer(player, amount - current)
            } else {
                it.withdrawPlayer(player, current - amount)
            }
        }
    }

    fun addBalance(uuid: UUID, amount: Double): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        return economy?.depositPlayer(player, amount)?.transactionSuccess() ?: false
    }

    fun addBalance(player: Player, amount: Double): Boolean {
        return economy?.depositPlayer(player, amount)?.transactionSuccess() ?: false
    }

    fun removeBalance(uuid: UUID, amount: Double): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        return economy?.withdrawPlayer(player, amount)?.transactionSuccess() ?: false
    }

    fun removeBalance(player: Player, amount: Double): Boolean {
        return economy?.withdrawPlayer(player, amount)?.transactionSuccess() ?: false
    }

    fun hasBalance(uuid: UUID, amount: Double): Boolean {
        return getBalance(uuid) >= amount
    }

    fun hasBalance(player: Player, amount: Double): Boolean {
        return getBalance(player) >= amount
    }

    fun isEnabled(): Boolean {
        return economy != null
    }
}

