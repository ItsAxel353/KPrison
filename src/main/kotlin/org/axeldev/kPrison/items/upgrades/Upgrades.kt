package org.axeldev.kPrison.items.upgrades

enum class Upgrades(
    val displayName: String,
    val description: String,
    val cost: Double,
    val keyName: String
) {
    SPEED(
        "Vitesse",
        "Augmente votre vitesse de déplacement.",
        100.0,
        "speed"
    ),
    FORTUNE(
        "Fortune",
        "Augmente les chances d'obtenir des ressources rares.",
        200.0,
        "fortune"
    ),
    EFFICIENCY(
        "Efficacité",
        "Augmente la vitesse de minage.",
        150.0,
        "efficiency"
    ),
}