package org.axeldev.kPrison

import org.axeldev.kPrison.core.Mine
import org.axeldev.kPrison.items.MineStick
import org.axeldev.kPrison.items.MineStickListener
import org.axeldev.kPrison.managers.EconomyManager
import org.axeldev.kPrison.managers.MineManager
import org.axeldev.kPrison.managers.PrisonerManager
import org.axeldev.kPrison.managers.RankManager
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class PrisonCommand(
    private val prisonerManager: PrisonerManager,
    private val rankManager: RankManager,
    private val mineManager: MineManager,
    private val economyManager: EconomyManager
) : CommandExecutor {

    private fun parseBlocks(blocksString: String): Map<org.bukkit.Material, Double> {
        val blocks = mutableMapOf<org.bukkit.Material, Double>()
        val parts = blocksString.split(",")
        for (part in parts) {
            val kv = part.split(":")
            if (kv.size == 2) {
                try {
                    val material = org.bukkit.Material.valueOf(kv[0].uppercase())
                    val weight = kv[1].toDouble()
                    blocks[material] = weight
                } catch (e: Exception) {
                    // Ignore invalid
                }
            }
        }
        return blocks
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Cette commande ne peut être utilisée que par un joueur.")
            return true
        }

        val player = sender
        val prisoner = prisonerManager.getPrisoner(player.uniqueId)

        when (args.getOrNull(0)?.lowercase()) {
            "balance", "bal" -> {
                val balance = economyManager.getBalance(player)
                player.sendMessage("Votre solde : ${String.format("%.2f", balance)} €")
            }

            "rank" -> {
                player.sendMessage("Votre rang : ${prisoner.Rank}")
            }

            "promote" -> {
                if (rankManager.promotePrisoner(prisoner, player)) {
                    prisonerManager.savePrisoner(prisoner)
                    player.sendTitle("§aFélicitations !", "Vous avez été promu au rang ${prisoner.Rank}.", 10, 70, 20)
                } else {
                    player.sendMessage("§cVous n'avez pas assez d'argent pour être promu.")
                }
            }

            "mine" -> {
                val mineId = args.getOrNull(1)
                if (mineId == null) {
                    player.sendMessage("Utilisation : /prison mine <id>")
                    return true
                }
                val mine = mineManager.getMine(mineId)
                if (mine == null) {
                    player.sendMessage("§cMine introuvable.")
                    return true
                }
                if (!rankManager.canPrisonerAccessMine(prisoner, mine.requiredRank)) {
                    player.sendMessage("§cVous n'avez pas le rang requis pour accéder à cette mine.")
                    return true
                }
                player.teleport(mine.teleportLocation)
                player.sendTitle("§aTéléporté à la mine ${mine.id}.", "", 10, 70, 20)
            }

            "reset", "resetmine" -> {
                // Vérifier la permission
                if (!player.hasPermission("prison.admin") && !player.isOp) {
                    player.sendMessage("§cVous n'avez pas la permission pour réinitialiser une mine.")
                    return true
                }
                val mineId = args.getOrNull(1)
                if (mineId == null) {
                    player.sendMessage("§cUtilisation : /prison reset <id>")
                    return true
                }
                if (mineManager.resetMine(mineId)) {
                    player.sendMessage("§aMine ${mineId} réinitialisée avec succès.")
                } else {
                    player.sendMessage("§cMine ${mineId} introuvable.")
                }
            }

            "setspawn" -> {
                // Vérifier la permission
                if (!player.hasPermission("prison.admin") && !player.isOp) {
                    player.sendMessage("§cVous n'avez pas la permission pour définir le point d'apparition d'une mine.")
                    return true
                }
                val mineId = args.getOrNull(1)
                if (mineId == null) {
                    player.sendMessage("§cUtilisation : /prison setspawn <id>")
                    return true
                }
                if (mineManager.setMineSpawn(mineId.uppercase(), player.location)) {
                    player.sendMessage("§aPoint d'apparition de la mine ${mineId.uppercase()} défini à votre position.")
                } else {
                    player.sendMessage("§cMine ${mineId.uppercase()} introuvable.")
                }
            }

            "createmine" -> {
                // Vérifier la permission
                if (!player.hasPermission("prison.admin") && !player.isOp) {
                    player.sendMessage("§cVous n'avez pas la permission pour créer une mine.")
                    return true
                }
                if (args.size < 3) {
                    player.sendMessage("§cUtilisation : /prison createmine <id> <stone:50,iron_ore:30>")
                    return true
                }
                val mineId = args[1].uppercase()
                val blocksString = args[2]
                val p1 = MineStickListener.getPos1(player.uniqueId.toString())
                val p2 = MineStickListener.getPos2(player.uniqueId.toString())
                if (p1 == null || p2 == null) {
                    player.sendMessage("§cVous devez définir pos1 et pos2 avec /prison pos1 et /prison pos2.")
                    return true
                }
                val blocks = parseBlocks(blocksString)
                if (blocks.isEmpty()) {
                    player.sendMessage("§cFormat des blocs invalide. Exemple : stone:50,iron_ore:30")
                    return true
                }
                val minX = min(p1.blockX, p2.blockX)
                val minY = min(p1.blockY, p2.blockY)
                val minZ = min(p1.blockZ, p2.blockZ)
                val maxX = max(p1.blockX, p2.blockX)
                val maxY = max(p1.blockY, p2.blockY)
                val maxZ = max(p1.blockZ, p2.blockZ)
                val centerX = (minX + maxX) / 2.0
                val centerY = (minY + maxY) / 2.0
                val centerZ = (minZ + maxZ) / 2.0
                val mine = Mine(
                    id = mineId,
                    requiredRank = mineId,
                    minX = minX,
                    minY = minY,
                    minZ = minZ,
                    maxX = maxX,
                    maxY = maxY,
                    maxZ = maxZ,
                    teleportLocation = Location(player.world, centerX, centerY, centerZ),
                    blocks = blocks,
                    resetDelay = 300
                )
                mineManager.addMine(mine)
                player.sendMessage("§aMine ${mineId} créée avec succès.")
            }

            "stick" -> {
                player.inventory.addItem(org.axeldev.kPrison.items.MineStick().creatMineStick())
                player.sendMessage("§a§lCLIQUE DROIT§r §apos1 §a§lCLIQUE GAUCHE§r §apos2")
            }

            "sellall" -> {
                var totalMoney = 0.0
                val itemPrices = mapOf(
                    org.bukkit.Material.COBBLESTONE to 1.0,
                    org.bukkit.Material.COAL to 5.0,
                    org.bukkit.Material.IRON_INGOT to 10.0,
                    org.bukkit.Material.GOLD_INGOT to 15.0,
                    org.bukkit.Material.DIAMOND to 20.0,
                    org.bukkit.Material.EMERALD to 25.0
                )

                val itemsToRemove = mutableListOf<org.bukkit.inventory.ItemStack>()
                for (item in player.inventory.contents) {
                    if (item != null && itemPrices.containsKey(item.type)) {
                        val price = itemPrices[item.type]!! * item.amount
                        totalMoney += price
                        itemsToRemove.add(item)
                    }
                }

                if (totalMoney > 0.0) {
                    itemsToRemove.forEach { player.inventory.removeItem(it) }
                    economyManager.addBalance(player, totalMoney)
                    player.sendMessage(
                        "§a✓ Vous avez vendu votre butin pour §6${
                            String.format(
                                "%.2f",
                                totalMoney
                            )
                        }€§a !"
                    )
                } else {
                    player.sendMessage("§cVous n'avez rien à vendre.")
                }
            }

            else -> {
                player.sendMessage("Commandes disponibles : /prison balance, /prison rank, /prison promote, /prison mine <id>, /prison reset <id>, /prison setspawn <id>, /prison createmine <id> <blocks>, /prison pickaxe, /prison upgrade, /prison sellall")
            }
        }
        return true
    }
}
