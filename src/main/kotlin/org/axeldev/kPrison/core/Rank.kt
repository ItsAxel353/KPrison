package org.axeldev.kPrison.core

data class Rank(
    val name: String,
    val level: Int,
    val requiredBalance: Double = 0.0,
    val permissions: List<String> = emptyList()
)
