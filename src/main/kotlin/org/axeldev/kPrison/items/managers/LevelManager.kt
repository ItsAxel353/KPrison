package org.axeldev.kPrison.items.managers

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.items.KPickaxe
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object LevelManager {

    fun KPickaxe.addXp(xp: Int): KPickaxe {
        val newXp = this.XP + xp
        val xpNeededForUp = this.level * 1000 // Example: each level requires 1000 XP

        return if (newXp >= xpNeededForUp) {
            this.copy(
                level = this.level + 1,
                XP = (newXp - xpNeededForUp).coerceAtLeast(0)
            )
        } else {
            this.copy(XP = newXp)
        }
    }

    fun handleBlockBreak(item: ItemStack) {
        val meta = item.itemMeta ?: return
        val container = meta.persistentDataContainer

        // 1. On récupère les données (si elles n'existent pas, on arrête)
        val currentLevel = container.get(KPrison.levelKey, PersistentDataType.INTEGER) ?: return
        val currentXp = container.get(KPrison.xpKey, PersistentDataType.INTEGER) ?: 0

        // 2. On utilise ta logique KPickaxe pour calculer
        val pickaxe = KPickaxe(level = currentLevel, durability = 100, XP = currentXp)
        val updated = pickaxe.addXp(10) // On ajoute 10 XP

        if (updated.level > currentLevel) {
            val newMaterial = getMaterialForLevel(updated.level)
            if (item.type != newMaterial) {
                item.type = newMaterial // On change l'item physiquement
            }
        }

        // 3. On enregistre les nouvelles données invisibles
        container.set(KPrison.levelKey, PersistentDataType.INTEGER, updated.level)
        container.set(KPrison.xpKey, PersistentDataType.INTEGER, updated.XP)

        // 4. On met à jour le visuel (Lore et Nom)
        meta.setDisplayName("§bKPickaxe | §fNiveau ${updated.level}")
        meta.lore = listOf("§7XP: §b${updated.XP} §8/ §7${updated.level * 1000}")

        item.itemMeta = meta
    }

    private fun getMaterialForLevel(level: Int): Material {
        return when (level) {
            in 1..4 -> Material.WOODEN_PICKAXE
            in 5..9 -> Material.STONE_PICKAXE
            in 10..14 -> Material.IRON_PICKAXE
            in 15..19 -> Material.GOLDEN_PICKAXE
            in 20..24 -> Material.DIAMOND_PICKAXE
            else -> Material.NETHERITE_PICKAXE
        }
    }
}