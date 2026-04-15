package org.axeldev.kPrison.core

import java.util.UUID

data class Prisoner(
    val uuid: UUID,
    val name: String = "Joueur",
    var balance: Double = 0.0,
    var Rank: String = "A"
)
