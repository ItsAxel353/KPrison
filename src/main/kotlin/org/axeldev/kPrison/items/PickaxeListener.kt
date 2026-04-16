package org.axeldev.kPrison.items

import org.axeldev.kPrison.KPrison
import org.axeldev.kPrison.items.managers.LevelManager
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.axeldev.kPrison.managers.PrisonerManager
import org.axeldev.kPrison.menus.PickaxeMenu.UpgradeGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
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
            val meta = item.itemMeta!!
            val durability = meta.persistentDataContainer.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 100
            if (durability <= 0) {
                player.sendMessage("§cVotre pioche est cassée ! Réparez-la ou obtenez-en une nouvelle.")
                event.isCancelled = true
                return
            }

            val block = event.block
            var (xpGain, durabilityLoss) = when (block.type) {
                Material.STONE -> 1 to 1
                Material.COBBLESTONE -> 1 to 1
                Material.COAL_ORE -> 5 to 2
                Material.IRON_ORE -> 10 to 3
                Material.GOLD_ORE -> 15 to 4
                Material.DIAMOND_ORE -> 20 to 5
                Material.EMERALD_ORE -> 25 to 6
                else -> 0 to 1
            }

            // Apply efficiency upgrade to reduce durability loss
            val efficiencyLvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[Upgrades.EFFICIENCY]!!, PersistentDataType.INTEGER) ?: 0
            durabilityLoss = maxOf(1, durabilityLoss - efficiencyLvl)

            LevelManager.handleBlockBreak(item, xpGain, durabilityLoss)
        }
        applyPassiveEffects(player, item)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title != "§8Amélioration de la Pioche") return
        event.isCancelled = true

        val player = event.whoClicked as Player
        val upgrade = UpgradeGUI.layout[event.slot]
        val itemInHand = player.inventory.itemInMainHand
        val meta = itemInHand.itemMeta ?: return

        if (upgrade != null) {
            // Handle upgrade
            val key = KPrison.upgradeKeys[upgrade]!!
            val container = meta.persistentDataContainer
            val currentLevel = container.get(key, PersistentDataType.INTEGER) ?: 0

            // 1. Calcul du prix (ex: 500 * niveau suivant)
            val price = (currentLevel + 1) * 500
            val prisoner = prisonerManager.getPrisoner(player.uniqueId)

            // 2. VÉRIFICATION ARGENT
            if (prisoner.balance < price) return player.sendMessage("Pas assez de sous !")

            // 3. Application de l'upgrade
            val nextLevel = currentLevel + 1
            container.set(key, PersistentDataType.INTEGER, nextLevel)

            // 4. Mise à jour de la Lore
            LevelManager.updatePickaxeLore(meta)

            itemInHand.itemMeta = meta
            player.sendMessage("§a[+] ${upgrade.displayName} est maintenant niveau $nextLevel !")

            // 5. On rafraîchit le menu
            UpgradeGUI.open(player)
        } else if (event.slot == 22) {
            // Handle repair
            val durability = meta.persistentDataContainer.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 100
            if (durability >= 100) return player.sendMessage("§cVotre pioche est déjà en parfait état !")

            val repairCost = (100 - durability) * 10
            val prisoner = prisonerManager.getPrisoner(player.uniqueId)

            if (prisoner.balance < repairCost) return player.sendMessage("Pas assez de sous !")

            prisoner.balance -= repairCost
            prisonerManager.savePrisoner(prisoner)

            meta.persistentDataContainer.set(KPrison.durabilityKey, PersistentDataType.INTEGER, 100)
            LevelManager.updatePickaxeLore(meta)
            itemInHand.itemMeta = meta

            player.sendMessage("§aVotre pioche a été réparée !")
            UpgradeGUI.open(player)
        }
    }

    @EventHandler
    fun onBlockDrop(event: BlockDropItemEvent) {
        val player = event.player ?: return
        val item = player.inventory.itemInMainHand

        if (item.hasItemMeta()) {
            val meta = item.itemMeta!!
            val fortuneLvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[Upgrades.FORTUNE]!!, PersistentDataType.INTEGER) ?: 0
            if (fortuneLvl > 0) {
                // Increase drops by fortune level (simple multiplier)
                event.items.forEach { drop ->
                    val amount = drop.itemStack.amount * (1 + fortuneLvl)
                    drop.itemStack.amount = amount
                }
            }
        }
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