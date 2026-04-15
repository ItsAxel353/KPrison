package org.axeldev.kPrison.database

import org.axeldev.kPrison.core.Mine
import org.axeldev.kPrison.core.Prisoner
import org.axeldev.kPrison.core.Rank
import org.axeldev.kPrison.config.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.Material
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager(private val dataFolder: File, private val configManager: ConfigManager) {

    private lateinit var connection: Connection

    fun connect() {
        try {
            val databaseType = configManager.getDatabaseType()

            when (databaseType.lowercase()) {
                "mariadb", "mysql" -> connectMariaDB()
                "sqlite" -> connectSQLite()
                else -> {
                    println("[KPrison] Type de base de données inconnu: $databaseType. Utilisation de MariaDB par défaut.")
                    connectMariaDB()
                }
            }

            println("[KPrison] Connexion à la base de données établie")
            createTables()
        } catch (e: SQLException) {
            println("[KPrison] Erreur de connexion à la base de données: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun connectMariaDB() {
        try {
            Class.forName("org.mariadb.jdbc.Driver")
            val host = configManager.getMySQLHost()
            val port = configManager.getMySQLPort()
            val database = configManager.getMySQLDatabase()
            val username = configManager.getMySQLUsername()
            val password = configManager.getMySQLPassword()
            val useSSL = configManager.getMySQLUseSSL()

            val url = "jdbc:mariadb://$host:$port/$database?useSSL=$useSSL&allowMultiQueries=true"
            connection = DriverManager.getConnection(url, username, password)
            println("[KPrison] Connecté à MariaDB: $host:$port/$database")
        } catch (e: ClassNotFoundException) {
            println("[KPrison] Driver MariaDB non trouvé: ${e.message}")
            throw e
        }
    }

    private fun connectSQLite() {
        val databaseFile = File(dataFolder, "kprison.db")
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
        println("[KPrison] Connecté à SQLite: ${databaseFile.absolutePath}")
    }

    fun disconnect() {
        try {
            connection.close()
            println("[KPrison] Déconnexion de la base de données")
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors de la déconnexion: ${e.message}")
        }
    }

    private fun createTables() {
        try {
            // Table des mines
            connection.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS mines (
                        id VARCHAR(255) PRIMARY KEY,
                        requiredRank VARCHAR(255) NOT NULL,
                        minX INT NOT NULL,
                        minY INT NOT NULL,
                        minZ INT NOT NULL,
                        maxX INT NOT NULL,
                        maxY INT NOT NULL,
                        maxZ INT NOT NULL,
                        world VARCHAR(255) NOT NULL,
                        teleportX DOUBLE NOT NULL,
                        teleportY DOUBLE NOT NULL,
                        teleportZ DOUBLE NOT NULL,
                        resetDelay INT NOT NULL,
                        lastReset BIGINT NOT NULL
                    )
                """)
            }
            println("[KPrison] ✓ Table 'mines' créée/vérifiée")

            // Table des blocs de la mine
            connection.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS mine_blocks (
                        mineId VARCHAR(255) NOT NULL,
                        material VARCHAR(255) NOT NULL,
                        weight DOUBLE NOT NULL,
                        PRIMARY KEY (mineId, material),
                        FOREIGN KEY (mineId) REFERENCES mines(id) ON DELETE CASCADE
                    )
                """)
            }
            println("[KPrison] ✓ Table 'mine_blocks' créée/vérifiée")

            // Table des prisonniers
            connection.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS prisoners (
                        uuid VARCHAR(255) PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        rank VARCHAR(255) NOT NULL,
                        balance DOUBLE NOT NULL DEFAULT 0.0,
                        lastUpdated BIGINT NOT NULL
                    )
                """)
            }
            println("[KPrison] ✓ Table 'prisoners' créée/vérifiée")

            // Table des rangs
            connection.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS ranks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        level INT NOT NULL,
                        requiredBalance DOUBLE NOT NULL,
                        permissions TEXT DEFAULT ''
                    )
                """)
            }
            println("[KPrison] ✓ Table 'ranks' créée/vérifiée")

            println("[KPrison] ✅ Toutes les tables ont été créées/vérifiées avec succès")
        } catch (e: SQLException) {
            println("[KPrison] ❌ Erreur lors de la création des tables: ${e.message}")
            e.printStackTrace()
        }
    }

    // ==================== MINES ====================

    fun saveMine(mine: Mine) {
        try {
            val world = mine.teleportLocation.world?.name ?: "world"
            connection.prepareStatement("""
                INSERT INTO mines 
                (id, requiredRank, minX, minY, minZ, maxX, maxY, maxZ, world, teleportX, teleportY, teleportZ, resetDelay, lastReset)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                requiredRank = VALUES(requiredRank),
                minX = VALUES(minX), minY = VALUES(minY), minZ = VALUES(minZ),
                maxX = VALUES(maxX), maxY = VALUES(maxY), maxZ = VALUES(maxZ),
                world = VALUES(world),
                teleportX = VALUES(teleportX), teleportY = VALUES(teleportY), teleportZ = VALUES(teleportZ),
                resetDelay = VALUES(resetDelay), lastReset = VALUES(lastReset)
            """).use { stmt ->
                stmt.setString(1, mine.id)
                stmt.setString(2, mine.requiredRank)
                stmt.setInt(3, mine.minX)
                stmt.setInt(4, mine.minY)
                stmt.setInt(5, mine.minZ)
                stmt.setInt(6, mine.maxX)
                stmt.setInt(7, mine.maxY)
                stmt.setInt(8, mine.maxZ)
                stmt.setString(9, world)
                stmt.setDouble(10, mine.teleportLocation.x)
                stmt.setDouble(11, mine.teleportLocation.y)
                stmt.setDouble(12, mine.teleportLocation.z)
                stmt.setInt(13, mine.resetDelay)
                stmt.setLong(14, mine.lastReset)
                stmt.executeUpdate()
            }

            // Sauvegarder les blocs
            connection.prepareStatement("DELETE FROM mine_blocks WHERE mineId = ?").use { stmt ->
                stmt.setString(1, mine.id)
                stmt.executeUpdate()
            }

            for ((material, weight) in mine.blocks) {
                connection.prepareStatement("""
                    INSERT INTO mine_blocks (mineId, material, weight) VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE weight = VALUES(weight)
                """).use { stmt ->
                    stmt.setString(1, mine.id)
                    stmt.setString(2, material.name)
                    stmt.setDouble(3, weight)
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors de la sauvegarde de la mine ${mine.id}: ${e.message}")
        }
    }

    fun loadMine(mineId: String): Mine? {
        return try {
            connection.prepareStatement("SELECT * FROM mines WHERE id = ?").use { stmt ->
                stmt.setString(1, mineId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val world = Bukkit.getWorld(rs.getString("world")) ?: return null
                    val blocks = mutableMapOf<Material, Double>()

                    connection.prepareStatement("SELECT * FROM mine_blocks WHERE mineId = ?").use { blockStmt ->
                        blockStmt.setString(1, mineId)
                        val blockRs = blockStmt.executeQuery()
                        while (blockRs.next()) {
                            try {
                                val material = Material.valueOf(blockRs.getString("material"))
                                val weight = blockRs.getDouble("weight")
                                blocks[material] = weight
                            } catch (e: Exception) {
                                // Ignore invalid materials
                            }
                        }
                    }

                    Mine(
                        id = rs.getString("id"),
                        requiredRank = rs.getString("requiredRank"),
                        minX = rs.getInt("minX"),
                        minY = rs.getInt("minY"),
                        minZ = rs.getInt("minZ"),
                        maxX = rs.getInt("maxX"),
                        maxY = rs.getInt("maxY"),
                        maxZ = rs.getInt("maxZ"),
                        teleportLocation = org.bukkit.Location(world, rs.getDouble("teleportX"), rs.getDouble("teleportY"), rs.getDouble("teleportZ")),
                        blocks = blocks,
                        resetDelay = rs.getInt("resetDelay"),
                        lastReset = rs.getLong("lastReset")
                    )
                } else null
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement de la mine $mineId: ${e.message}")
            null
        }
    }

    fun loadAllMines(): List<Mine> {
        val mines = mutableListOf<Mine>()
        try {
            connection.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT id FROM mines")
                while (rs.next()) {
                    loadMine(rs.getString("id"))?.let { mines.add(it) }
                }
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement des mines: ${e.message}")
        }
        return mines
    }

    fun deleteMine(mineId: String) {
        try {
            connection.prepareStatement("DELETE FROM mines WHERE id = ?").use { stmt ->
                stmt.setString(1, mineId)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors de la suppression de la mine $mineId: ${e.message}")
        }
    }

    // ==================== PRISONERS ====================

    fun savePrisoner(prisoner: Prisoner) {
        try {
            connection.prepareStatement("""
                INSERT INTO prisoners (uuid, name, rank, balance, lastUpdated)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                name = VALUES(name), rank = VALUES(rank), balance = VALUES(balance), lastUpdated = VALUES(lastUpdated)
            """).use { stmt ->
                stmt.setString(1, prisoner.uuid.toString())
                stmt.setString(2, prisoner.name)
                stmt.setString(3, prisoner.Rank)
                stmt.setDouble(4, prisoner.balance)
                stmt.setLong(5, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors de la sauvegarde du prisonnier ${prisoner.name}: ${e.message}")
        }
    }

    fun loadPrisoner(uuid: String): Prisoner? {
        return try {
            connection.prepareStatement("SELECT * FROM prisoners WHERE uuid = ?").use { stmt ->
                stmt.setString(1, uuid)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    Prisoner(
                        uuid = java.util.UUID.fromString(uuid),
                        name = rs.getString("name"),
                        Rank = rs.getString("rank"),
                        balance = rs.getDouble("balance")
                    )
                } else null
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement du prisonnier $uuid: ${e.message}")
            null
        }
    }

    fun loadAllPrisoners(): List<Prisoner> {
        val prisoners = mutableListOf<Prisoner>()
        try {
            connection.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT uuid FROM prisoners")
                while (rs.next()) {
                    loadPrisoner(rs.getString("uuid"))?.let { prisoners.add(it) }
                }
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement des prisonniers: ${e.message}")
        }
        return prisoners
    }

    // ==================== RANKS ====================

    fun saveRank(rank: Rank) {
        try {
            connection.prepareStatement("""
                INSERT INTO ranks (name, level, requiredBalance, permissions)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                level = VALUES(level), requiredBalance = VALUES(requiredBalance), permissions = VALUES(permissions)
            """).use { stmt ->
                stmt.setString(1, rank.name)
                stmt.setInt(2, rank.level)
                stmt.setDouble(3, rank.requiredBalance)
                stmt.setString(4, rank.permissions.joinToString(","))
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors de la sauvegarde du rang ${rank.name}: ${e.message}")
        }
    }

    fun loadRank(rankName: String): Rank? {
        return try {
            connection.prepareStatement("SELECT * FROM ranks WHERE name = ?").use { stmt ->
                stmt.setString(1, rankName)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val permissions = rs.getString("permissions")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                    Rank(
                        name = rs.getString("name"),
                        level = rs.getInt("level"),
                        requiredBalance = rs.getDouble("requiredBalance"),
                        permissions = permissions
                    )
                } else null
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement du rang $rankName: ${e.message}")
            null
        }
    }

    fun loadAllRanks(): List<Rank> {
        val ranks = mutableListOf<Rank>()
        try {
            connection.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM ranks ORDER BY level ASC")
                while (rs.next()) {
                    val permissions = rs.getString("permissions")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                    ranks.add(Rank(
                        name = rs.getString("name"),
                        level = rs.getInt("level"),
                        requiredBalance = rs.getDouble("requiredBalance"),
                        permissions = permissions
                    ))
                }
            }
        } catch (e: SQLException) {
            println("[KPrison] Erreur lors du chargement des rangs: ${e.message}")
        }
        return ranks
    }
}
