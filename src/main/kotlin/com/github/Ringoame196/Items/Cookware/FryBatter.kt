package com.github.Ringoame196.Items.Cookware

import com.github.Ringoame196.Cook
import com.github.Ringoame196.Data.CookData
import com.github.Ringoame196.Items.FoodManager
import com.github.Ringoame196.Items.Item
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player

class FryBatter {
    private val foodManager = FoodManager()
    private val cook = Cook()
    private val cookData = CookData()
    private val itemClass = Item()
    fun dressing(player: Player, entity: ItemFrame) {
        val item = player.inventory.itemInMainHand
        if (foodManager.isExpirationDateHasExpired(player, entity.item)) { return }
        val dressingItem = cookData.dressing(item) ?: return
        if (!cook.isCookLevel(dressingItem.itemMeta?.displayName?:return, player)) {
            return
        }
        itemClass.reduceMainItem(player)
        player.inventory.addItem(dressingItem)
        val particleLocation = entity.location.add(0.0, 1.0, 0.0)
        entity.world.spawnParticle(Particle.EXPLOSION_HUGE, particleLocation, 1)
        player.world.playSound(player.location, Sound.ITEM_BUCKET_EMPTY, 1f, 1f)
    }
}
