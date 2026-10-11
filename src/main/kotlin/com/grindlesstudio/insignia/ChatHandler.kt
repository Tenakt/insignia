//package com.grindlesstudio.insignia
//
//import net.minecraft.network.chat.Component
//import net.neoforged.bus.api.SubscribeEvent
//import net.neoforged.fml.common.EventBusSubscriber
//import net.neoforged.neoforge.event.ServerChatEvent
//
//@EventBusSubscriber(modid = Insignia.MOD_ID)
//object ChatHandler {
//
//    @SubscribeEvent
//    @JvmStatic    fun onServerChat(event: ServerChatEvent) {
//        val player = event.player
//        val rawPrefix = PermissionManager.getPlayerPrefix(player.uuid)
//
//        if (rawPrefix.isNotEmpty()) {
//            val prefixComponent = ColorUtils.parseColor("$rawPrefix ")
//
//            val formattedMessage = Component.empty()
//                .append(prefixComponent)
//                .append(player.displayName)
//                .append(": ")
//                .append(event.message)
//
//            // Заменяем исходное сообщение чата
//            event.message = formattedMessage
//        }
//    }
//}