package org.axeldev.kPrison.items

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

data class KPickaxe(
    val level: Int,
    val durability: Int,
    val XP: Int,
    val enchantments: List<Upgrades> = emptyList()
)

class KPickaxeItem {
    fun donnerPioche(joueur: Player): ItemStack {
        val item = ItemStack(Material.WOODEN_PICKAXE)
        val meta = item.itemMeta ?: return item

        // On cache le niveau 1 et l'XP 0 dans l'item
        meta.persistentDataContainer.set(KPrison.levelKey, PersistentDataType.INTEGER, 1)
        meta.persistentDataContainer.set(KPrison.xpKey, PersistentDataType.INTEGER, 0)
        // meta.persistentDataContainer.set(KPrison.durabilityKey, PersistentDataType.INTEGER, 100)

        meta.setDisplayName("§bKPickaxe | §fNiveau 1")
        meta.lore = listOf("§7XP: 0 | Durabilité: 100")
        meta.isUnbreakable = true
        item.itemMeta = meta

        joueur.inventory.addItem(item)
        return item
    }

}
