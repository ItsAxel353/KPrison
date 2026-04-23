package org.axeldev.kPrison.items

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MineStick {
    fun creatMineStick(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD)
        val meta = item.itemMeta
        if (meta != null) {
            meta.setDisplayName("§6§lMine Stick")
            val lore = mutableListOf<String>()
            lore.add("§7Utilisez ce bâton pour")
            lore.add("§7définir les mines et les positions dans le monde.")
            meta.lore = lore

            meta.addEnchant(Enchantment.INFINITY, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

            item.itemMeta = meta
        }

        return item
    }
}