package org.axeldev.kPrison.items

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.core.Prisoner
import org.axeldev.kPrison.items.managers.LevelManager
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.axeldev.kPrison.managers.PrisonerManager
import org.axeldev.kPrison.menus.PickaxeMenu.UpgradeGUI
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PickaxeListener(private val prisonerManager: PrisonerManager) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (item.hasItemMeta()) {
            LevelManager.handleBlockBreak(item)
        }
        applyPassiveEffects(player, item)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title != "§8Amélioration de la Pioche") return
        event.isCancelled = true

        val player = event.whoClicked as Player
        val upgrade = UpgradeGUI.layout[event.slot] ?: return // Récupère l'upgrade selon le slot
        val itemInHand = player.inventory.itemInMainHand
        val meta = itemInHand.itemMeta ?: return

        val key = KPrison.upgradeKeys[upgrade]!!
        val container = meta.persistentDataContainer
        val currentLevel = container.get(key, PersistentDataType.INTEGER) ?: 0

        // 1. Calcul du prix (ex: 500 * niveau suivant)
        val price = (currentLevel + 1) * 500
        val prisoner = prisonerManager.getPrisoner(player.uniqueId)


        // 2. VÉRIFICATION ARGENT (À adapter selon ton plugin d'économie)
        if (prisoner.balance < price) return player.sendMessage("Pas assez de sous !")

        // 3. Application de l'upgrade
        val nextLevel = currentLevel + 1
        container.set(key, PersistentDataType.INTEGER, nextLevel)

        // 4. Mise à jour de la Lore de manière propre
        updatePickaxeLore(meta)

        itemInHand.itemMeta = meta
        player.sendMessage("§a[+] ${upgrade.displayName} est maintenant niveau $nextLevel !")

        // 5. On rafraîchit le menu pour mettre à jour les prix affichés
        UpgradeGUI.open(player)
    }

    fun updatePickaxeLore(meta: ItemMeta) {
        val newLore = mutableListOf<String>()

        // On remet les stats de base
        val level = meta.persistentDataContainer.get(KPrison.levelKey, PersistentDataType.INTEGER) ?: 1
        val xp = meta.persistentDataContainer.get(KPrison.xpKey, PersistentDataType.INTEGER) ?: 0
        newLore.add("§7XP: §b$xp §8| §7Niveau: §f$level")
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

    fun applyPassiveEffects(player: Player, item: ItemStack) {
        val meta = item.itemMeta ?: return
        val container = meta.persistentDataContainer

        // SPEED -> Donne l'effet Hâte (Fast Digging)
        val speedLvl = container.get(KPrison.upgradeKeys[Upgrades.SPEED]!!, PersistentDataType.INTEGER) ?: 0
        if (speedLvl > 0) {
            // On donne l'effet pour 2 secondes (on rafraîchit tant qu'il tient l'item)
            player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 40, speedLvl - 1, false, false))
        }
    }

}