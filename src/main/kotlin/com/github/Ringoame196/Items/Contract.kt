package com.github.Ringoame196

import com.github.Ringoame196.Data.Money
import com.github.Ringoame196.Entity.AoringoPlayer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Contract {
    fun request(player: Player, money: Int) {
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta as BookMeta
        meta.setDisplayName("${ChatColor.YELLOW}契約書[契約待ち]")
        val bookMessage = meta.getPage(1)
            .replace("甲方：[プレイヤー名]\nUUID：[UUID]", "甲方：${player.name}\nUUID：${player.uniqueId}")
            .replace("取引金額：[値段]", "取引金額：${money}円")
        meta.setPage(1, bookMessage)
        item.setItemMeta(meta)
        player.inventory.setItemInMainHand(item)
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
    }
    fun contract(player: AoringoPlayer, money: Int) {
        val sender = player.player
        val item = sender.inventory.itemInMainHand
        val meta = item.itemMeta as BookMeta
        val bookMessage = meta.getPage(1)
        val priceIndex = bookMessage.indexOf("取引金額：")
        val priceMessage = bookMessage.substring(priceIndex + "取引金額：".length).replace("円", "").toInt()
        if (money != priceMessage) {
            player.sendErrorMessage("金額が違います")
            return
        }
        if (Money().get(player.uniqueId.toString()) < money.toInt()) {
            player.sendErrorMessage("お金が足りません")
            return
        }
        Money().remove(player.uniqueId.toString(), money, false)
        val setBookMessage = writeContractDate(meta, sender, money)
        meta.setPage(1, setBookMessage)
        item.setItemMeta(meta)
        sender.inventory.setItemInMainHand(item)
        sender.playSound(player.player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
    }
    fun writeContractDate(meta: BookMeta, player: Player, money: Int): String {
        val currentDate = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(dateFormatter)
        meta.setDisplayName("${ChatColor.RED}契約本@${money}円契約")
        val setBookMessage = meta.getPage(1)
            .replace("乙方：[プレイヤー名]\nUUID：[UUID]", "乙方：${player.name}\nUUID：${player.uniqueId}")
            .replace("契約日：[日付]", "契約日：$formattedDate")
        return setBookMessage
    }
    fun returnMoney(player: Player) {
        val item = player.inventory.itemInMainHand
        val bookMessage = item.itemMeta as BookMeta
        if (!bookMessage.getPage(1).contains("UUID：${player.uniqueId}")) {
            return
        }
        val money = item.itemMeta?.displayName?.replace("${ChatColor.RED}契約本@", "")?.replace("円契約", "")?.toInt()
        Money().add(player.uniqueId.toString(), money ?: return, false)
        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
    }
    fun copyBlock(item: ItemStack, player: Player): ItemStack {
        val meta = item.itemMeta as BookMeta
        val currentDate = LocalDate.now()

        // 日付を指定したフォーマットで文字列として取得
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(dateFormatter)
        meta.setPage(
            1,
            "${ChatColor.DARK_RED}STOP COPYING\n\n" +
                "『契約書の複製は、青りんごサーバーの規約により禁止されています。』\n\n\n" +
                "プレイヤー名:${player.name}\n" +
                "日にち:$formattedDate"
        )
        item.setItemMeta(meta)
        return item
    }
}
