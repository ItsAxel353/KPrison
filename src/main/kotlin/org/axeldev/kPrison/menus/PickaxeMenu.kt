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
            10 to Upgrades.EFFICIENCY,
            12 to Upgrades.FORTUNE,
            14 to Upgrades.UNBREAKING,
            16 to Upgrades.SILK_TOUCH,
            28 to Upgrades.EXPLOSIVE,
        )

        fun open(player: Player) {
            val inv = Bukkit.createInventory(null, 36, "§8Amélioration de la Pioche")
            val itemInHand = player.inventory.itemInMainHand
            val meta = itemInHand.itemMeta

            layout.forEach { (slot, type) ->
                // On récupère le niveau actuel sur la pioche pour l'afficher dans le menu
                val currentLevel =
                    meta?.persistentDataContainer?.get(KPrison.upgradeKeys[type]!!, PersistentDataType.INTEGER) ?: 0

                val displayItem = ItemStack(getMaterialForType(type))
                val dMeta = displayItem.itemMeta
                dMeta?.setDisplayName("§6${type.displayName}")

                val lore = mutableListOf<String>()
                lore.add("§7${type.description}")
                lore.add("")
                lore.add("§7Niveau: §e${currentLevel}/${type.maxLevel}")
                if (currentLevel < type.maxLevel) {
                    val price = (currentLevel + 1) * 1000
                    lore.add("§7Prix upgrade: §6${price}€")
                    lore.add("")
                    lore.add("§a▶ Clique pour améliorer")
                } else {
                    lore.add("§7Status: §cMAX LEVEL")
                }

                dMeta?.lore = lore
                displayItem.itemMeta = dMeta
                inv.setItem(slot, displayItem)
            }

            // Add repair option
            val repairItem = ItemStack(Material.ANVIL)
            val rMeta = repairItem.itemMeta
            rMeta?.setDisplayName("§bRéparer la Pioche")
            val durability = meta?.persistentDataContainer?.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 1000
            rMeta?.lore = listOf(
                "§7Durabilité: §e$durability/1000",
                "",
                if (durability >= 1000) {
                    "§7Status: §cParfait état"
                } else {
                    "§a▶ Clique pour réparer"
                }
            )
            repairItem.itemMeta = rMeta
            inv.setItem(31, repairItem)

            player.openInventory(inv)
        }

        private fun getMaterialForType(type: Upgrades) = when (type) {
            Upgrades.EFFICIENCY -> Material.IRON_PICKAXE
            Upgrades.FORTUNE -> Material.GOLD_INGOT
            Upgrades.UNBREAKING -> Material.NETHERITE_INGOT
            Upgrades.SILK_TOUCH -> Material.FEATHER
            Upgrades.EXPLOSIVE -> Material.TNT
        }
    }
}