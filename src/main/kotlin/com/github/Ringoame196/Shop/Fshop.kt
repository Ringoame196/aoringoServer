package com.github.Ringoame196.Shop

import com.github.Ringoame196.Account
import com.github.Ringoame196.Data.WorldGuard
import com.github.Ringoame196.Entity.AoringoPlayer
import com.github.Ringoame196.Items.Item
import com.github.Ringoame196.MoneyUseCase
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Barrel
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class Fshop(private val shop: ItemFrame) : Account {
    private val shopInfo: String = shop.customName ?: throw RuntimeException("ショップ情報を取得できませんでした")
    private val moneyUseCase = MoneyUseCase()
    override fun getAccountID(): String {
        return acquisitionAccountName()
    }
    fun isOwner(player: Player): Boolean {
        val location = shop.location
        return WorldGuard().getOwnerOfRegion(location)?.contains(player.uniqueId) == true || WorldGuard().getMemberOfRegion(location)?.contains(player.uniqueId) == true
    }
    private fun acquisitionAccountName(): String {
        val userIDStartIndex = shopInfo.indexOf("userID:") + 7
        val userIDEndIndex = shopInfo.indexOf(",", userIDStartIndex)

        if (userIDStartIndex < 0 || userIDEndIndex < 0) {
            throw RuntimeException("口座IDの取得に失敗しました")
        }

        return shopInfo.substring(userIDStartIndex, userIDEndIndex)
    }
    private fun acquisitionPrice(): Int {
        val index = shopInfo.indexOf("price:")

        if (index < 0) {
            throw RuntimeException("価格の取得に失敗しました")
        }

        val priceSubstring = shopInfo.substring(index + 6)
        return priceSubstring.toIntOrNull() ?: throw RuntimeException("価格の変換に失敗しました")
    }
    fun buyGUI(item: ItemStack): Inventory {
        val gui = Bukkit.createInventory(null, 9, "${ChatColor.BLUE}Fショップ")
        val price = acquisitionPrice()
        gui.setItem(0, Item().make(Material.COMPASS, "ショップ", shop.uniqueId.toString()))
        gui.setItem(3, item)
        gui.setItem(4, Item().make(Material.EMERALD_BLOCK, "${ChatColor.GREEN}購入", "${price}円"))
        return gui
    }
    fun buy(aoringoPlayer: AoringoPlayer, item: ItemStack) {
        val sender = aoringoPlayer.player
        val price = acquisitionPrice()
        if (shop.item != item) {
            aoringoPlayer.sendErrorMessage("売り物が更新されました")
            return
        }
        if (moneyUseCase.getMoney(aoringoPlayer.playerAccount) < price) {
            aoringoPlayer.sendErrorMessage("お金が足りません")
            return
        }
        sender.inventory.addItem(item)
        moneyUseCase.tradeMoney(aoringoPlayer, this, price)
        sender.sendMessage("${ChatColor.GREEN}購入しました")
        sender.playSound(sender, Sound.BLOCK_ANVIL_USE, 1f, 1f)
        replenishment()
        if (shop.item.type == Material.AIR) {
            sender.closeInventory()
        } else { sender.openInventory(buyGUI(shop.item)) }
    }
    private fun replenishment() {
        val block = shop.location.add(0.0, -1.0, 0.0).block
        if (block.type != Material.BARREL) { return }
        val barrel = block.state as Barrel
        shop.setItem(ItemStack(Material.AIR))
        for (item in barrel.inventory) {
            item ?: continue
            shop.setItem(item)
            item.amount = item.amount - 1
            return
        }
    }
}
