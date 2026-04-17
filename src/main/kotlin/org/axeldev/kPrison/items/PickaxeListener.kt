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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
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
            val durability = meta.persistentDataContainer.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 1000
            if (durability <= 0) {
                player.sendMessage("§c✗ Votre pioche est cassée ! Réparez-la.")
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

            // Apply unbreaking upgrade to reduce durability loss
            val unbreakingLvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[Upgrades.UNBREAKING]!!, PersistentDataType.INTEGER) ?: 0
            if (unbreakingLvl > 0) {
                // Chaque niveau d'indestructibilité réduit de 20% la perte (diviser par 1.2, 1.4, 1.6)
                durabilityLoss = (durabilityLoss / (1.0 + (unbreakingLvl * 0.2))).toInt().coerceAtLeast(1)
            }

            // Apply fortune upgrade - multiplie les drops
            val fortuneLvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[Upgrades.FORTUNE]!!, PersistentDataType.INTEGER) ?: 0
            if (fortuneLvl > 0) {
                val multiplier = 1 + (fortuneLvl * 0.5)
                block.drops.forEach { drop ->
                    drop.amount = (drop.amount * multiplier).toInt().coerceAtLeast(1)
                }
            }

            // Apply explosive upgrade - mine les blocs alentours
            val explosiveLvl = meta.persistentDataContainer.get(KPrison.upgradeKeys[Upgrades.EXPLOSIVE]!!, PersistentDataType.INTEGER) ?: 0
            if (explosiveLvl > 0) {
                val radius = explosiveLvl
                val world = block.world
                val centerX = block.x
                val centerY = block.y
                val centerZ = block.z

                for (x in (centerX - radius)..(centerX + radius)) {
                    for (y in (centerY - radius)..(centerY + radius)) {
                        for (z in (centerZ - radius)..(centerZ + radius)) {
                            val nearbyBlock = world.getBlockAt(x, y, z)
                            if (nearbyBlock != block && nearbyBlock.type != Material.AIR) {
                                nearbyBlock.breakNaturally(item)
                            }
                        }
                    }
                }
            }

            LevelManager.handleBlockBreak(item, xpGain, durabilityLoss)
        }
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

            // Vérifier si on a atteint le niveau max
            if (currentLevel >= upgrade.maxLevel) {
                player.sendMessage("§cCet upgrade est déjà au niveau maximum !")
                return
            }

            // 1. Calcul du prix (ex: 1000 * niveau suivant)
            val price = (currentLevel + 1) * 1000
            val prisoner = prisonerManager.getPrisoner(player.uniqueId)

            // 2. VÉRIFICATION ARGENT
            if (prisoner.balance < price) return player.sendMessage("§cPas assez d'argent ! Il te manque ${price - prisoner.balance}€")

            // 3. Enlever l'argent du joueur
            prisoner.balance -= price
            prisonerManager.savePrisoner(prisoner)

            // 4. Application de l'upgrade
            val nextLevel = currentLevel + 1
            container.set(key, PersistentDataType.INTEGER, nextLevel)

            // 5. Mise à jour de la Lore
            LevelManager.updatePickaxeLore(meta)

            itemInHand.itemMeta = meta
            player.sendMessage("§a✓ ${upgrade.displayName} amélioré au niveau $nextLevel !")

            // 6. On rafraîchit le menu
            UpgradeGUI.open(player)
        } else if (event.slot == 31) {
            // Handle repair
            val durability = meta.persistentDataContainer.get(KPrison.durabilityKey, PersistentDataType.INTEGER) ?: 1000
            if (durability >= 1000) return player.sendMessage("§c✗ Votre pioche est déjà en parfait état !")

            meta.persistentDataContainer.set(KPrison.durabilityKey, PersistentDataType.INTEGER, 1000)
            LevelManager.updatePickaxeLore(meta)
            itemInHand.itemMeta = meta

            player.sendMessage("§a✓ Votre pioche a été réparée !")
            UpgradeGUI.open(player)
        }

        fun applyPassiveEffects(player: Player, item: ItemStack) {
            val meta = item.itemMeta ?: return
            val container = meta.persistentDataContainer

            // EFFICIENCY -> Donne l'effet Hâte (Fast Digging)
            val efficiencyLvl =
                container.get(KPrison.upgradeKeys[Upgrades.EFFICIENCY]!!, PersistentDataType.INTEGER) ?: 0
            if (efficiencyLvl > 0) {
                // Chaque niveau = niveau de Hâte
                player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 60, efficiencyLvl - 1, false, false))
            }
        }
    }
}