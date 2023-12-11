package com.github.Ringoame196.Smartphones

import com.github.Ringoame196.APK
import com.github.Ringoame196.APKs
import com.github.Ringoame196.Data.Money
import com.github.Ringoame196.Data.WorldGuard
import com.github.Ringoame196.Items.Item
import com.github.Ringoame196.ResourcePack
import com.github.Ringoame196.Scoreboard
import com.github.Ringoame196.Smartphone.APKs.ItemProtectionAPK
import com.github.Ringoame196.Smartphone.APKs.LandPurchase
import com.github.Ringoame196.Smartphones.APKs.ConversionMoneyAPK
import com.github.Ringoame196.Smartphones.APKs.EnderChestAPK
import com.github.Ringoame196.Smartphones.APKs.HealthCcareAPK
import com.github.Ringoame196.Smartphones.APKs.LandProtectionAPK
import com.github.Ringoame196.Smartphones.APKs.OPAPK
import com.github.Ringoame196.Smartphones.APKs.PlayerRatingAPK
import com.github.Ringoame196.Smartphones.APKs.SortAPK
import com.github.Ringoame196.Smartphones.APKs.TeleportAPK
import com.github.Ringoame196.Yml
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class Smartphone {
    val apkList = mapOf<String,APKs>(
        "${ChatColor.YELLOW}エンダーチェスト" to EnderChestAPK(),
        "${ChatColor.GREEN}所持金変換" to ConversionMoneyAPK(),
        "${ChatColor.RED}アイテム保護" to ItemProtectionAPK(),
        "${ChatColor.GREEN}テレポート" to TeleportAPK(),
        "${ChatColor.GREEN}プレイヤー評価" to PlayerRatingAPK(),
        "${ChatColor.GREEN}土地保護" to LandProtectionAPK(),
        "${ChatColor.YELLOW}OP用" to OPAPK(),
        "${ChatColor.YELLOW}アプリ並べ替え" to SortAPK(),
        "${ChatColor.AQUA}ヘルスケア" to HealthCcareAPK()
    )
    fun createGUI(plugin: Plugin, player: Player): Inventory {
        val gui = Bukkit.createInventory(null, 27, "${ChatColor.BLUE}スマートフォン")
        val smartphoneSlots = mutableListOf(1, 3, 5, 7, 10, 12, 14, 16, 19, 21, 23, 25)
        val playerHaveAPKList = Yml().getList(plugin, "playerData", player.uniqueId.toString(), "apkList")
        if (playerHaveAPKList.isNullOrEmpty()) {
            return gui
        }

        val apkCount = minOf(smartphoneSlots.size, playerHaveAPKList.size)
        for (i in 0 until apkCount) {
            val apkName = playerHaveAPKList[i]
            val customModelData = apkList[apkName]?.customModelData?:0
            gui.setItem(smartphoneSlots[i], Item().make(Material.GREEN_CONCRETE, "${ChatColor.YELLOW}[アプリ]$apkName", customModelData = customModelData))
        }
        return gui
    }
    fun startUpAKS(player: Player, item: ItemStack, plugin: Plugin, shift: Boolean) {
        val itemName = item.itemMeta?.displayName
        val apkName = itemName?.replace("${ChatColor.YELLOW}[アプリ]", "") ?: return
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
        if (shift && item.type == Material.GREEN_CONCRETE) {
            APK().remove(player, apkName, item.itemMeta?.customModelData ?: 0, plugin)
            player.openInventory(createGUI(plugin, player))
            return
        }
        apkList[apkName]?.openGUI(player,plugin)
        teleportWorldFromPlayer(player,apkName)
        if (item.type == Material.EMERALD && (item.itemMeta?.customModelData ?: return) >= 1) {
            if ((item.itemMeta?.customModelData ?: return) > 4) { return }
            val money = itemName.replace("${ChatColor.GREEN}", "").replace("円", "").toInt()
            moneyItem(player, money, item)
        }
    }
    private fun getWorldSpawnLocation(worldName:String): Location? {
        return Bukkit.getWorld(worldName)?.spawnLocation
    }
    private fun teleportWorldFromPlayer(player:Player,worldName:String){
        val worldID = mapOf<String,String>(
            "${ChatColor.GOLD}ロビー" to "world",
            "${ChatColor.GREEN}生活ワールド" to "Home",
            "${ChatColor.AQUA}資源ワールド" to "Survival",
            "${ChatColor.YELLOW}ショップ" to "shop"
        )
        val playerLocation = player.location
        val location = getWorldSpawnLocation(worldID[worldName] ?:"world")
        player.teleport(location?:playerLocation)
    }
    fun opClick(item: ItemStack, plugin: Plugin, shift: Boolean, player: org.bukkit.entity.Player) {
        when (item.itemMeta?.displayName) {
            "${ChatColor.RED}ショップ保護リセット" -> {
                if (!shift) { return }
                val list = Yml().getList(plugin, "conservationLand", "", "protectedName") ?: return
                for (name in list) {
                    if (Scoreboard().getValue("protectionContract", name) == 2) {
                        Scoreboard().reduce("protectionContract", name, 1)
                        continue
                    }
                    WorldGuard().reset(name, Bukkit.getWorld("shop") ?: return)
                    Yml().removeToList(plugin, "", "conservationLand", "protectedName", name)
                }
                Bukkit.broadcastMessage("${ChatColor.RED}[ショップ] ショップの購入土地がリセットされました")
            }
            "${ChatColor.YELLOW}リソパ更新" -> ResourcePack(plugin).update()
            "${ChatColor.GREEN}運営ギフトリセット" -> {
                if (!Scoreboard().existence("admingift")) { return }
                Scoreboard().delete("admingift")
                Scoreboard().make("admingift", "admingift")
                Bukkit.broadcastMessage("${ChatColor.YELLOW}[青りんごサーバー] 運営ギフトがリセットされました")
            }
            "${ChatColor.GREEN}テストワールド" -> player.teleport(Bukkit.getWorld("testworld")?.spawnLocation ?: return)
        }
    }
    fun wgClick(item: ItemStack, plugin: Plugin, player: org.bukkit.entity.Player, shift: Boolean) {
        val playerClass = com.github.Ringoame196.Entity.AoringoPlayer(player)
        if (player.world.name != "Home" && !player.isOp) {
            playerClass.sendErrorMessage("保護は生活ワールドのみ使用可能です")
            player.closeInventory()
            return
        }
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
        when (item.itemMeta?.displayName) {
            "${ChatColor.GOLD}木の斧ゲット" -> player.inventory.addItem(ItemStack(Material.WOODEN_AXE))
            "${ChatColor.AQUA}保護一覧" -> {
                player.closeInventory()
                LandPurchase().listRegionsInWorld(player)
            }
            "${ChatColor.YELLOW}保護作成" -> {
                player.closeInventory()
                player.addScoreboardTag("rg")
                player.sendMessage("${ChatColor.AQUA}[土地保護]保護名を入力してください")
            }
            "${ChatColor.GREEN}情報" -> {
                val gui = player.openInventory.topInventory
                gui.setItem(2, Item().make(Material.MAP, "${ChatColor.YELLOW}保護情報",))
                gui.setItem(4, Item().make(Material.PLAYER_HEAD, "${ChatColor.AQUA}メンバー追加"))
                gui.setItem(6, Item().make(Material.PLAYER_HEAD, "${ChatColor.RED}メンバー削除"))
                gui.setItem(8, Item().make(Material.REDSTONE_BLOCK, "${ChatColor.RED}削除", "${ChatColor.DARK_RED}シフトで実行"))
            }
            "${ChatColor.YELLOW}保護情報" -> {
                player.closeInventory()
                player.sendMessage("${ChatColor.YELLOW}-----保護情報-----")
                player.sendMessage("${ChatColor.GOLD}保護名:${WorldGuard().getName(player.location)}")
                player.sendMessage("${ChatColor.YELLOW}オーナー:" + if (WorldGuard().getOwnerOfRegion(player.location)?.contains(player.uniqueId) == true) { "${ChatColor.GOLD}あなたはオーナーです" } else { "${ChatColor.RED}あなたはオーナーではありません" })
                player.sendMessage("${ChatColor.AQUA}メンバー:" + if (WorldGuard().getMemberOfRegion(player.location)?.contains(player.uniqueId) == true) { "${ChatColor.GOLD}あなたはメンバーです" } else { "${ChatColor.RED}あなたはメンバーではありません" })
            }
            "${ChatColor.AQUA}メンバー追加" -> {
                if (WorldGuard().getOwnerOfRegion(player.location)?.contains(player.uniqueId) != true) {
                    playerClass.sendErrorMessage("自分の保護土地内で実行してください")
                    return
                }
                LandPurchase().addMemberGUI(player, WorldGuard().getName(player.location))
            }
            "${ChatColor.RED}メンバー削除" -> {
                if (WorldGuard().getOwnerOfRegion(player.location)?.contains(player.uniqueId) != true) {
                    playerClass.sendErrorMessage("自分の保護土地内で実行してください")
                    return
                }
                LandPurchase().removeMemberGUI(player, WorldGuard().getName(player.location) ?: return)
            }
            "${ChatColor.RED}削除" -> {
                if (WorldGuard().getOwnerOfRegion(player.location)?.contains(player.uniqueId) != true) {
                    playerClass.sendErrorMessage("自分の保護土地内で実行してください")
                    return
                }
                if (!shift) { return }
                WorldGuard().delete(player, WorldGuard().getName(player.location) ?: return)
                player.sendMessage("${ChatColor.RED}保護を削除しました")
            }
        }
    }
    fun createProtectionGUI(player: Player, name: String): Inventory {
        val price = LandPurchase().price(player)
        val gui = Bukkit.createInventory(null, 9, "${ChatColor.BLUE}保護設定($name)")
        gui.setItem(4, Item().make(Material.GREEN_WOOL, "${ChatColor.GREEN}作成", "${price}円"))
        return gui
    }
    fun protection(player: org.bukkit.entity.Player, item: ItemStack, name: String) {
        val price = item.itemMeta?.lore?.get(0)?.replace("円", "")?.toInt() ?: return
        val world = player.world
        if ((Money().get(player.uniqueId.toString())) < price) {
            com.github.Ringoame196.Entity.AoringoPlayer(player).sendErrorMessage("お金が足りません")
            return
        }
        player.performCommand("/expand vert")
        player.performCommand("rg claim $name")
        if (WorldGuard().getProtection(world, name)) {
            player.sendMessage("${ChatColor.GREEN}[WG]正常に保護をかけました")
            Money().remove(player.uniqueId.toString(), price, true)
            player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
        }
        player.closeInventory()
    }
    private fun moneyItem(player: Player, money: Int, item: ItemStack) {
        if ((Money().get(player.uniqueId.toString())) < money) {
            com.github.Ringoame196.Entity.AoringoPlayer(player).sendErrorMessage("お金が足りません")
        } else {
            val giveItem = item.clone()
            giveItem.amount = 1
            player.inventory.addItem(giveItem)
            Money().remove(player.uniqueId.toString(), money, true)
        }
        player.closeInventory()
    }
}
