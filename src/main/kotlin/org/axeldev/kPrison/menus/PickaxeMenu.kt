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
            15 to Upgrades.EFFICIENCY,
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

            // Add repair option
            val repairItem = ItemStack(Material.ANVIL)
            val rMeta = repairItem.itemMeta
            rMeta?.setDisplayName("§bRéparer la Pioche")
            val durability = meta?.persistentDataContainer?.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 100
            val repairCost = (100 - durability) * 10 // 10 per missing durability
            rMeta?.lore = listOf(
                "§7Durabilité actuelle: §e$durability/100",
                "§7Prix: §6${repairCost} tokens",
                "",
                "§aClique pour réparer !"
            )
            repairItem.itemMeta = rMeta
            inv.setItem(22, repairItem)

            player.openInventory(inv)
        }

        private fun getMaterialForType(type: Upgrades) = when (type) {
            Upgrades.SPEED -> Material.FEATHER
            Upgrades.FORTUNE -> Material.GOLD_INGOT
            Upgrades.EFFICIENCY -> Material.DIAMOND_PICKAXE
            else -> {
                Material.BARRIER
            }
        }
    }
}