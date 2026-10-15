package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@EventBusSubscriber(modid = Insignia.MOD_ID)
object TabHandler {

    private var tickCounter = 0

    @JvmStatic
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        updateHeaderFooter(player)
        updatePlayerTab(player)
    }

    @JvmStatic
    @SubscribeEvent
    fun onTabListNameFormat(event: PlayerEvent.TabListNameFormat) {
        val player = event.entity as? ServerPlayer ?: return
        val tagComponent = PermissionManager.getFormattedTag(player.uuid, isChat = false)

        event.displayName = Component.empty()
            .append(tagComponent)
            .append(player.name)
    }

    @JvmStatic
    @SubscribeEvent
    fun onServerChat(event: ServerChatEvent) {
        val player = event.player
        val tagComponent = PermissionManager.getFormattedTag(player.uuid, isChat = true)

        val formattedMessage = Component.empty()
            .append(tagComponent)
            .append(player.name)
            .append("§f: ")
            .append(event.message)

        player.server.playerList.broadcastSystemMessage(formattedMessage, false)
        event.isCanceled = true
    }

    @JvmStatic
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        tickCounter++
        if (tickCounter < 20) return
        tickCounter = 0

        val server = event.server
        server.playerList.players.forEach { updateHeaderFooter(it) }
    }

    private fun updateHeaderFooter(player: ServerPlayer) {
        val server = player.server
        val onlinePlayers = server.playerList.playerCount
        val onlineStaff = server.playerList.players.count { it.hasPermissions(4) }
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMem = runtime.maxMemory() / (1024 * 1024)
        val ping = player.connection.latency()

        // Преобразуем цветовые коды & в § для названия сервера
        val serverNameFormatted = PermissionManager.storage.serverName.replace("&", "§")

        val headerText = """
            $serverNameFormatted
            §7>> §bWelcome, §3${player.name.string}§b! §7<<
            §eOnline players: §f$onlinePlayers
            §eOnline staff: §f$onlineStaff
        """.trimIndent()

        val footerText = """
            §8- §f${player.name.string} §7Ping: §a$ping
            §7Date §a$currentDate
            §aPing: $ping
            §7Used memory: §a$usedMem MB §7/ §a$maxMem MB
        """.trimIndent()

        player.setTabListHeaderFooter(
            Component.literal(headerText),
            Component.literal(footerText)
        )
    }

    fun updatePlayerTab(player: ServerPlayer) {
        player.refreshTabListName()
        player.refreshDisplayName()

        val packet = ClientboundPlayerInfoUpdatePacket(
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            player
        )
        player.server.playerList.broadcastAll(packet)
    }

    fun updateAllPlayers(server: MinecraftServer) {
        server.playerList.players.forEach {
            updateHeaderFooter(it)
            updatePlayerTab(it)
        }
    }
}