package org.axeldev.kPrison.items.upgrades

enum class Upgrades(
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val keyName: String
) {
    EFFICIENCY(
        "Efficacité",
        "Augmente la vitesse de minage",
        5,
        "efficiency"
    ),
    FORTUNE(
        "Fortune",
        "Multiplie les drops de minerai",
        3,
        "fortune"
    ),
    UNBREAKING(
        "Indestructibilité",
        "Réduit la perte de durabilité",
        3,
        "unbreaking"
    ),
    SILK_TOUCH(
        "Silk Touch",
        "Mine les blocs sans transformation",
        1,
        "silk_touch"
    ),
    EXPLOSIVE(
        "Explosif",
        "Mine en zone circulaire",
        3,
        "explosive"
    ),
}