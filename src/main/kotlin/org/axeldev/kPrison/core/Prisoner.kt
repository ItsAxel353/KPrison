package org.axeldev.kPrison.core

import java.util.UUID

data class Prisoner(
    val uuid: UUID,
    val name: String = "Joueur",
    var Rank: String = "A"
)
