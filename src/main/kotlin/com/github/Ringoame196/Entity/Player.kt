package com.github.Ringoame196.Entity

import com.github.Ringoame196.Job.Job
import com.github.Ringoame196.Scoreboard
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.math.pow
import kotlin.math.sqrt

class Player {
    data class PlayerData(
        var titleMoneyBossbar: BossBar? = null,
        var speedMeasurement: Boolean = false
    )
    fun setName(player: Player) {
        val jobID = Scoreboard().getValue("job", player.uniqueId.toString())
        val jobColor = mutableListOf("", "${ChatColor.DARK_PURPLE}", "${ChatColor.DARK_RED}", "${ChatColor.GRAY}")
        player.setDisplayName("${jobColor[jobID]}${player.displayName}@${Job().get(player)}")
        player.setPlayerListName("${jobColor[jobID]}${player.playerListName}")
        if (player.isOp) {
            player.setDisplayName("${ChatColor.YELLOW}[運営]" + player.displayName)
            player.setPlayerListName("${ChatColor.YELLOW}[運営]" + player.playerListName)
        }
    }
    private fun levelupMessage(player: Player, message: String) {
        player.sendMessage(message)
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }
    fun addPower(player: Player) {
        Scoreboard().add("status_Power", player.uniqueId.toString(), 1)
        levelupMessage(player, "${ChatColor.YELLOW}パワーアップ！！")
    }
    fun addMaxHP(player: Player) {
        Scoreboard().add("status_HP", player.uniqueId.toString(), 1)
        levelupMessage(player, "${ChatColor.RED}最大HPアップ！！")
        player.maxHealth = 20.0 + Scoreboard().getValue("status_HP", player.uniqueId.toString())
    }
    fun sendActionBar(player: Player, message: String) {
        val actionBarMessage = ChatColor.translateAlternateColorCodes('&', message)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(actionBarMessage))
    }
    fun getPlayersInRadius(center: Location, radius: Double): List<Player>? {
        val playersInRadius = mutableListOf<Player>()

        for (player in center.world?.players ?: return null) {
            val playerLocation = player.location
            val distance = center.distance(playerLocation)

            if (distance <= radius) {
                // 半径内にいるプレイヤーをリストに追加
                playersInRadius.add(player)
            }
        }

        return playersInRadius
    }
    fun permission(player: Player, plugin: Plugin, permission: String, allow: Boolean) {
        val permissions = player.addAttachment(plugin) // "plugin" はプラグインのインスタンスを指します
        permissions.setPermission(permission, allow)
        player.recalculatePermissions()
    }
    fun setTab(player: Player) {
        player.playerListHeader = "${ChatColor.AQUA}青りんごサーバー"
        player.playerListFooter = "${ChatColor.YELLOW}" + when (player.world.name) {
            "world" -> "ロビーワールド"
            "Survival" -> "資源ワールド"
            "Nether" -> "ネザー"
            "shop" -> "ショップ"
            "event" -> "イベントワールド"
            "Home" -> "建築ワールド"
            else -> "${ChatColor.RED}未設定"
        }
    }
    fun setProtectionPermission(player: Player, plugin: Plugin) {
        permission(
            player, plugin, "blocklocker.protect",
            when (player.world.name) {
                "Survival" -> true
                "Home" -> true
                else -> false
            }
        )
    }
    fun calculateDistance(pos1: Location, pos2: Location): Int {
        val deltaX = (pos1.x - pos2.x).toDouble()
        val deltaY = (pos1.y - pos2.y).toDouble()
        val deltaZ = (pos1.z - pos2.z).toDouble()

        // 3D空間での距離を計算

        return sqrt(deltaX.pow(2) + deltaZ.pow(2)).toInt()
    }
}
