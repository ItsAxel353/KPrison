package org.axeldev.kPrison.menus

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object PickaxeMenu {
    object UpgradeGUI {
        val layout = mapOf(
            11 to Upgrades.SPEED,
            13 to Upgrades.FORTUNE,
        )

        fun open(player: Player) {
            val inv = Bukkit.createInventory(null, 27, "§8Amélioration de la Pioche")
            val itemInHand = player.inventory.itemInMainHand
            val meta = itemInHand.itemMeta

            layout.forEach { (slot, type) ->
                // On récupère le niveau actuel sur la pioche pour l'afficher dans le menu
                val currentLevel =
                    meta?.persistentDataContainer?.get(KPrison.upgradeKeys[type]!!, PersistentDataType.INTEGER) ?: 0

                val displayItem = ItemStack(getMaterialForType(type))
                val dMeta = displayItem.itemMeta
                dMeta?.setDisplayName("§b${type.displayName}")
                dMeta?.lore = listOf(
                    "§7Niveau actuel: §e$currentLevel",
                    "§7Prix: §6${(currentLevel + 1) * 500} tokens",
                    "",
                    "§aClique pour améliorer !"
                )
                displayItem.itemMeta = dMeta
                inv.setItem(slot, displayItem)
            }
            player.openInventory(inv)
        }

        private fun getMaterialForType(type: Upgrades) = when (type) {
            Upgrades.SPEED -> Material.FEATHER
            Upgrades.FORTUNE -> Material.GOLD_INGOT
            else -> {
                Material.BARRIER
            }
        }
    }
}