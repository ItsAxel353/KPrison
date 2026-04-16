package org.axeldev.kPrison

import org.axeldev.kPrison.config.ConfigManager
import org.axeldev.kPrison.database.DatabaseManager
import org.axeldev.kPrison.items.PickaxeListener
import org.axeldev.kPrison.items.upgrades.Upgrades
import org.axeldev.kPrison.listeners.MiningListener
import org.axeldev.kPrison.listeners.PlayerJoinListener
import org.axeldev.kPrison.managers.MineManager
import org.axeldev.kPrison.managers.RankManager
import org.axeldev.kPrison.managers.PrisonerManager
import org.axeldev.kPrison.managers.ScoreBoardManager
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class KPrison : JavaPlugin() {

    lateinit var configManager: ConfigManager
    lateinit var databaseManager: DatabaseManager
    lateinit var mineManager: MineManager
    lateinit var rankManager: RankManager
    lateinit var prisonerManager: PrisonerManager
    lateinit var scoreBoardManager: ScoreBoardManager

    companion object {
        lateinit var levelKey: NamespacedKey
        lateinit var xpKey: NamespacedKey
        lateinit var durabilityKey: NamespacedKey

        lateinit var upgradeKeys: Map<Upgrades, NamespacedKey>
    }

    override fun onEnable() {
        // Charger la configuration
        configManager = ConfigManager(dataFolder)
        configManager.loadConfig()
        println("[KPrison] Configuration chargée avec succès")
        // Initialiser la base de données
        databaseManager = DatabaseManager(dataFolder, configManager)
        databaseManager.connect()

        levelKey = NamespacedKey(this, "pick_level")
        xpKey = NamespacedKey(this, "pick_xp")
        durabilityKey = NamespacedKey(this, "pick_durability")
        upgradeKeys = Upgrades.entries.associateWith { NamespacedKey(this, it.keyName) }

        // Plugin startup logic
        mineManager = MineManager(databaseManager)

        rankManager = RankManager()
        prisonerManager = PrisonerManager(databaseManager)
        scoreBoardManager = ScoreBoardManager(prisonerManager, rankManager)

        // Enregistrer la commande
        getCommand("prison")?.setExecutor(PrisonCommand(prisonerManager, rankManager, mineManager))

        // Enregistrer le listener
        server.pluginManager.registerEvents(MiningListener(mineManager, prisonerManager, rankManager), this)
        server.pluginManager.registerEvents(PlayerJoinListener(scoreBoardManager), this)
        server.pluginManager.registerEvents(PickaxeListener(prisonerManager), this)

        // Mettre à jour le scoreboard en temps réel (toutes les 0.5 secondes = 10 ticks)
        object : BukkitRunnable() {
            override fun run() {
                scoreBoardManager.updateAllScoreboards()
            }
        }.runTaskTimer(this, 0L, 10L)

        // Planifier la vérification des réinitialisations de mines toutes les minutes
        object : BukkitRunnable() {
            override fun run() {
                mineManager.resetMinesIfDue()
            }
        }.runTaskTimer(this, 0L, 1200L) // Toutes les 60 secondes (1200 ticks)
    }

    override fun onDisable() {
        // Déconnecter la base de données
        databaseManager.disconnect()
        println("[KPrison] Plugin désactivé avec succès")
    }
}
