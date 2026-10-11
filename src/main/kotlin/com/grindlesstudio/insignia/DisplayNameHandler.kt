package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

@EventBusSubscriber(modid = Insignia.MOD_ID)
object DisplayNameHandler {

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
    fun onTabListNameFormat(event: PlayerEvent.TabListNameFormat) {
        val tagComponent = PermissionManager.getFormattedTag(event.entity.uuid, isChat = false)

        event.displayName = Component.empty()
            .append(tagComponent)
            .append(event.entity.name)
    }
}