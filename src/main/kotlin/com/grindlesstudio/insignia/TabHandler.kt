package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
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

    private val dateFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Обновляем информацию игрока после входа.
     */
    @SubscribeEvent
    @JvmStatic
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return

        updateHeaderFooter(player)
        updatePlayerTab(player)
    }

    /**
     * Формируем имя игрока в таб-листе.
     */
    @SubscribeEvent
    @JvmStatic
    fun onTabListNameFormat(event: PlayerEvent.TabListNameFormat) {
        val player = event.entity as? ServerPlayer ?: return

        val tag = PermissionManager.getFormattedTag(
            player.uuid,
            isChat = false
        )

        event.displayName = Component.empty()
            .append(tag)
            .append(player.name)
    }

    /**
     * Форматируем сообщения чата.
     *
     * Пока сохраняем прежнюю схему обработки, чтобы не менять
     * поведение чата без проверки API конкретной версии NeoForge.
     */
    @SubscribeEvent
    @JvmStatic
    fun onServerChat(event: ServerChatEvent) {
        val player = event.player

        val tag = PermissionManager.getFormattedTag(
            player.uuid,
            isChat = true
        )

        val formattedMessage = Component.empty()
            .append(tag)
            .append(player.name)
            .append(Component.literal("§f: "))
            .append(event.message)

        player.server.playerList.broadcastSystemMessage(
            formattedMessage,
            false
        )

        event.isCanceled = true
    }

    /**
     * Обновляем заголовок и подвал раз в секунду.
     */
    @SubscribeEvent
    @JvmStatic
    fun onServerTick(event: ServerTickEvent.Post) {
        tickCounter++

        if (tickCounter < 20) return
        tickCounter = 0

        event.server.playerList.players.forEach { player ->
            updateHeaderFooter(player)
        }
    }

    /**
     * Обновляет заголовок и подвал таб-листа конкретного игрока.
     */
    private fun updateHeaderFooter(player: ServerPlayer) {
        val server = player.server
        val playerList = server.playerList

        val onlinePlayers = playerList.playerCount
        val onlineStaff = playerList.players.count {
            it.hasPermissions(4)
        }

        val currentDate = LocalDate.now().format(dateFormatter)

        val runtime = Runtime.getRuntime()
        val usedMemory =
            (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemory =
            runtime.maxMemory() / (1024 * 1024)

        val ping = player.connection.latency()

        val serverName = ColorUtils.parseColor(
            PermissionManager.storage.serverName
        )

        val header = Component.empty()
            .append(serverName)
            .append(Component.literal("\n"))
            .append(
                Component.literal(
                    "§7>> §bWelcome, §3${player.name.string}§b! §7<<"
                )
            )
            .append(Component.literal("\n"))
            .append(
                Component.literal(
                    "§eOnline players: §f$onlinePlayers"
                )
            )
            .append(Component.literal("\n"))
            .append(
                Component.literal(
                    "§eOnline staff: §f$onlineStaff"
                )
            )

        val footer = Component.literal(
            """
            §8- §f${player.name.string} §7Ping: §a$ping ms
            §7Date: §a$currentDate
            §7Used memory: §a$usedMemory MB §7/ §a$maxMemory MB
            """.trimIndent()
        )

        player.setTabListHeaderFooter(header, footer)
    }

    /**
     * Обновляет имя игрока в таб-листе.
     */
    fun updatePlayerTab(player: ServerPlayer) {
        player.refreshTabListName()
        player.refreshDisplayName()
    }

    /**
     * Обновляет таб-лист и заголовок/подвал для всех игроков.
     */
    fun updateAllPlayers(server: MinecraftServer) {
        server.playerList.players.forEach { player ->
            updateHeaderFooter(player)
            updatePlayerTab(player)
        }
    }
}