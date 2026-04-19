package org.axeldev.kPrison.items.managers

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.items.KPickaxe
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
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

    fun handleBlockBreak(item: ItemStack, xpGain: Int = 10, durabilityLoss: Int = 1) {
        val meta = item.itemMeta ?: return
        val container = meta.persistentDataContainer

        // 1. On récupère les données (si elles n'existent pas, on arrête)
        val currentLevel = container.get(KPrison.levelKey, PersistentDataType.INTEGER) ?: return
        val currentXp = container.get(KPrison.xpKey, PersistentDataType.INTEGER) ?: 0
        val currentDurability = container.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 1000

        // 2. Calculer la nouvelle durabilité
        val newDurability = (currentDurability - durabilityLoss).coerceAtLeast(0)

        // 3. On utilise ta logique KPickaxe pour calculer
        val pickaxe = KPickaxe(level = currentLevel, durability = newDurability, XP = currentXp)
        val updated = pickaxe.addXp(xpGain)

        // 4. On enregistre les nouvelles données invisibles
        container.set(KPrison.levelKey, PersistentDataType.INTEGER, updated.level)
        container.set(KPrison.xpKey, PersistentDataType.INTEGER, updated.XP)
        container.set(KPrison.durabilityKey, PersistentDataType.INTEGER, updated.durability)

        // 5. On met à jour le visuel (Lore et Nom)
        meta.setDisplayName("§bKPickaxe | §fNiveau ${updated.level}")
        updatePickaxeLore(meta)

        item.itemMeta = meta
    }

    fun updatePickaxeLore(meta: ItemMeta) {
        val newLore = mutableListOf<String>()

        // On remet les stats de base
        val level = meta.persistentDataContainer.get(KPrison.levelKey, PersistentDataType.INTEGER) ?: 1
        val xp = meta.persistentDataContainer.get(KPrison.xpKey, PersistentDataType.INTEGER) ?: 0
        val durability = meta.persistentDataContainer.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 100
        newLore.add("§7XP: §b$xp §8/ §7${level * 1000}")
        newLore.add("§7Durabilité: §b$durability §8/ §71000")
        newLore.add("")
        newLore.add("§6§lAméliorations:")

        // On ajoute chaque upgrade si son niveau est > 0
        Upgrades.entries.forEach { type ->
            val lvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[type]!!, PersistentDataType.INTEGER) ?: 0
            if (lvl > 0) {
                newLore.add(" §8• §b${type.displayName}: §eNiveau $lvl")
            }
        }

        meta.lore = newLore
    }
}