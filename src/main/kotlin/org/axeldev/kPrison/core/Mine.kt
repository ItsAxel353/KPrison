package org.axeldev.kPrison.core

import org.bukkit.Location
import org.bukkit.Material

data class Mine(
    val id: String,
    val requiredRank: String,
    val minX: Int,
    val minY: Int,
    val minZ: Int,
    val maxX: Int,
    val maxY: Int,
    val maxZ: Int,
    val teleportLocation: Location,
    val blocks: Map<Material, Double>,
    val resetDelay: Int,
    var lastReset: Long = System.currentTimeMillis()
    ) {

    fun isResetDue(): Boolean {
        return System.currentTimeMillis() - lastReset >= resetDelay * 1000L
    }

    fun reset() {
        lastReset = System.currentTimeMillis()
    }

    fun getTotalBlockWeight(): Double {
        return blocks.values.sum()
    }

    fun getRandomMaterial(): Material? {
        val total = getTotalBlockWeight()
        if (total == 0.0) return null
        val random = kotlin.random.Random.nextDouble() * total
        var cumulative = 0.0
        for ((material, weight) in blocks) {
            cumulative += weight
            if (random <= cumulative) return material
        }
        return null
    }
}
