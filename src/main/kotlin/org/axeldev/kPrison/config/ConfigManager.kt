package org.axeldev.kPrison.config

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(private val dataFolder: File) {

    private lateinit var config: YamlConfiguration
    private val configFile = File(dataFolder, "config.yml")

    fun loadConfig() {
        // Créer le dossier s'il n'existe pas
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        // Copier le fichier config par défaut si absent
        if (!configFile.exists()) {
            val defaultConfig = this::class.java.getResourceAsStream("/config.yml")
            if (defaultConfig != null) {
                configFile.writeBytes(defaultConfig.readBytes())
                println("[KPrison] Fichier config.yml créé avec succès")
            }
        }

        // Charger la configuration
        config = YamlConfiguration.loadConfiguration(configFile)
        println("[KPrison] Configuration chargée avec succès")
    }

    // ==================== DATABASE ====================

    fun getDatabaseType(): String = config.getString("database.type", "sqlite") ?: "sqlite"

    fun getMySQLHost(): String = config.getString("database.mysql.host", "localhost") ?: "localhost"

    fun getMySQLPort(): Int = config.getInt("database.mysql.port", 3306)

    fun getMySQLDatabase(): String = config.getString("database.mysql.database", "kprison") ?: "kprison"

    fun getMySQLUsername(): String = config.getString("database.mysql.username", "root") ?: "root"

    fun getMySQLPassword(): String = config.getString("database.mysql.password", "password") ?: "password"

    fun getMySQLUseSSL(): Boolean = config.getBoolean("database.mysql.useSSL", false)

    // ==================== MINES ====================

    fun getDefaultResetDelay(): Int = config.getInt("mines.defaultResetDelay", 300)

    fun showResetMessages(): Boolean = config.getBoolean("mines.showResetMessages", true)

    // ==================== RANKS ====================

    fun isRanksEnabled(): Boolean = config.getBoolean("ranks.enabled", true)

    fun getRanksList(): Map<String, Pair<Double, String>> {
        val ranks = mutableMapOf<String, Pair<Double, String>>()
        val rankList = config.getStringList("ranks.list")

        for (rankString in rankList) {
            val parts = rankString.split(":")
            if (parts.size == 3) {
                val name = parts[0]
                val requiredBalance = parts[1].toDoubleOrNull() ?: 0.0
                val displayName = parts[2]
                ranks[name] = Pair(requiredBalance, displayName)
            }
        }

        return ranks
    }

    // ==================== ECONOMY ====================

    fun getBlockReward(materialName: String): Double {
        return config.getDouble("economy.rewards.$materialName", 0.0)
    }

    fun getCurrencySymbol(): String = config.getString("economy.currencySymbol", "€") ?: "€"

    fun getInitialBalance(): Double = config.getDouble("economy.initialBalance", 0.0)

    // ==================== SCOREBOARD ====================

    fun showScoreboardOnJoin(): Boolean = config.getBoolean("scoreboard.showOnJoin", true)

    fun getScoreboardUpdateFrequency(): Long = config.getLong("scoreboard.updateFrequency", 10L)

    fun showScoreboardEmojis(): Boolean = config.getBoolean("scoreboard.showEmojis", true)

    // ==================== MESSAGES ====================

    fun getPrefix(): String = config.getString("messages.prefix", "§6[KPrison]§r") ?: "§6[KPrison]§r"

    fun getErrorMessage(errorKey: String): String {
        return config.getString("messages.errors.$errorKey", "") ?: ""
    }

    fun getSuccessMessage(successKey: String): String {
        return config.getString("messages.success.$successKey", "") ?: ""
    }

    fun getInfoMessage(infoKey: String): String {
        return config.getString("messages.info.$infoKey", "") ?: ""
    }

    // ==================== LOGS ====================

    fun isLoggingEnabled(): Boolean = config.getBoolean("logs.enabled", true)

    fun shouldLogType(logType: String): Boolean {
        val logTypes = config.getStringList("logs.logTypes")
        return logTypes.contains(logType)
    }

    // ==================== ADVANCED ====================

    fun shouldOptimizeDatabase(): Boolean = config.getBoolean("advanced.optimizeDatabase", true)

    fun useCache(): Boolean = config.getBoolean("advanced.useCache", true)

    fun getAutoSaveInterval(): Int = config.getInt("advanced.autoSaveInterval", 5)

    fun isDebugEnabled(): Boolean = config.getBoolean("advanced.debug", false)
}

