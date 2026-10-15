package com.grindlesstudio.insignia

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent

@EventBusSubscriber(modid = Insignia.MOD_ID)
object InsigniaCommands {

    private val COLOR_SUGGESTIONS = SuggestionProvider<CommandSourceStack> { _, builder ->
        SharedSuggestionProvider.suggest(ColorUtils.COLOR_MAP.keys, builder)
    }

    private fun isRussian(source: CommandSourceStack): Boolean {
        val player = source.player ?: return true
        return LanguageUtils.getLanguage(source).startsWith("ru")
    }

    private fun isEnglish(source: CommandSourceStack): Boolean {
        val player = source.player ?: return true
        return !LanguageUtils.getLanguage(source).startsWith("ru")
    }

    private fun executeDisplay(source: CommandSourceStack, slot: String, mode: String): Int {
        val player = source.playerOrException

        if (PermissionManager.setDisplayMode(player.uuid, slot, mode)) {
            TabHandler.updatePlayerTab(player)

            val slotTranslated = LanguageUtils.getSlotName(source, slot)
            val modeTranslated = LanguageUtils.getModeName(source, mode)

            source.sendSuccess(
                { Component.literal(LanguageUtils.tr(source, "msg.display.success", slotTranslated, modeTranslated)) },
                false
            )
        } else {
            source.sendFailure(Component.literal(LanguageUtils.tr(source, "msg.display.invalid")))
        }
        return 1
    }

    @JvmStatic
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        val dispatcher = event.dispatcher
        val rootNode = Commands.literal("insignia")

        // 1. СПРАВКА / HELP
        rootNode.then(
            Commands.literal("справка")
                .requires { isRussian(it) }
                .executes { sendHelp(it.source, "игрок"); 1 }
                .then(Commands.literal("игрок").executes { sendHelp(it.source, "игрок"); 1 })
                .then(Commands.literal("админ").executes { sendHelp(it.source, "админ"); 1 })
        )

        rootNode.then(
            Commands.literal("help")
                .requires { isEnglish(it) }
                .executes { sendHelp(it.source, "player"); 1 }
                .then(Commands.literal("player").executes { sendHelp(it.source, "player"); 1 })
                .then(Commands.literal("admin").executes { sendHelp(it.source, "admin"); 1 })
        )

        // 2. ВСТУПИТЬ ИЛИ СМЕНИТЬ ГРУППУ / JOIN
        val joinAction = Commands.argument("group_name", StringArgumentType.word())
            .executes { ctx ->
                val player = ctx.source.playerOrException
                val groupName = StringArgumentType.getString(ctx, "group_name")

                if (PermissionManager.storage.groups.containsKey(groupName.lowercase())) {
                    PermissionManager.setPlayerGroup(player.uuid, groupName)
                    TabHandler.updatePlayerTab(player)
                    ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.join.success", groupName)) }, false)
                } else {
                    ctx.source.sendFailure(Component.literal(LanguageUtils.tr(ctx.source, "msg.join.fail", groupName)))
                }
                1
            }

        rootNode.then(Commands.literal("вступить").requires { isRussian(it) }.then(joinAction))
        rootNode.then(Commands.literal("join").requires { isEnglish(it) }.then(joinAction))

        // 3. ВЫЙТИ ИЗ ГРУППЫ ИЛИ СБРОСИТЬ ПРЕФИКС / LEAVE
        rootNode.then(
            Commands.literal("выйти")
                .requires { isRussian(it) }
                .then(
                    Commands.literal("группа").executes { ctx ->
                        val player = ctx.source.playerOrException
                        PermissionManager.removePlayerGroup(player.uuid)
                        TabHandler.updatePlayerTab(player)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.leave.group.success")) }, false)
                        1
                    }
                )
                .then(
                    Commands.literal("префикс").executes { ctx ->
                        val player = ctx.source.playerOrException
                        PermissionManager.removePlayerPrefix(player.uuid)
                        TabHandler.updatePlayerTab(player)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.leave.prefix.success")) }, false)
                        1
                    }
                )
        )

        rootNode.then(
            Commands.literal("leave")
                .requires { isEnglish(it) }
                .then(
                    Commands.literal("group").executes { ctx ->
                        val player = ctx.source.playerOrException
                        PermissionManager.removePlayerGroup(player.uuid)
                        TabHandler.updatePlayerTab(player)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.leave.group.success")) }, false)
                        1
                    }
                )
                .then(
                    Commands.literal("prefix").executes { ctx ->
                        val player = ctx.source.playerOrException
                        PermissionManager.removePlayerPrefix(player.uuid)
                        TabHandler.updatePlayerTab(player)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.leave.prefix.success")) }, false)
                        1
                    }
                )
        )

        // 4. ПОКАЗ / DISPLAY
        rootNode.then(
            Commands.literal("показ")
                .requires { isRussian(it) }
                .then(
                    Commands.literal("чат")
                        .then(Commands.literal("префикс").executes { executeDisplay(it.source, "chat", "prefix") })
                        .then(Commands.literal("группа").executes { executeDisplay(it.source, "chat", "group") })
                        .then(Commands.literal("выкл").executes { executeDisplay(it.source, "chat", "none") })
                )
                .then(
                    Commands.literal("таб")
                        .then(Commands.literal("префикс").executes { executeDisplay(it.source, "tab", "prefix") })
                        .then(Commands.literal("группа").executes { executeDisplay(it.source, "tab", "group") })
                        .then(Commands.literal("выкл").executes { executeDisplay(it.source, "tab", "none") })
                )
        )

        rootNode.then(
            Commands.literal("display")
                .requires { isEnglish(it) }
                .then(
                    Commands.literal("chat")
                        .then(Commands.literal("prefix").executes { executeDisplay(it.source, "chat", "prefix") })
                        .then(Commands.literal("group").executes { executeDisplay(it.source, "chat", "group") })
                        .then(Commands.literal("none").executes { executeDisplay(it.source, "chat", "none") })
                )
                .then(
                    Commands.literal("tab")
                        .then(Commands.literal("prefix").executes { executeDisplay(it.source, "tab", "prefix") })
                        .then(Commands.literal("group").executes { executeDisplay(it.source, "tab", "group") })
                        .then(Commands.literal("none").executes { executeDisplay(it.source, "tab", "none") })
                )
        )

        // 5. СОЗДАТЬ / CREATE (АДМИН)
        val createGroupNode = Commands.argument("name", StringArgumentType.word())
            .then(
                Commands.argument("color", StringArgumentType.word())
                    .suggests(COLOR_SUGGESTIONS)
                    .executes { ctx ->
                        val name = StringArgumentType.getString(ctx, "name")
                        val color = StringArgumentType.getString(ctx, "color")

                        PermissionManager.createGroup(name, color)
                        TabHandler.updateAllPlayers(ctx.source.server)

                        val group = PermissionManager.storage.groups[name.lowercase()]

                        ctx.source.sendSuccess(
                            {
                                Component.literal(LanguageUtils.tr(ctx.source, "msg.create_group.success", name))
                                    .append(ColorUtils.parseColor(group?.text ?: ""))
                            },
                            true
                        )
                        1
                    }
            )

        val createPrefixNode = Commands.argument("name", StringArgumentType.word())
            .then(
                Commands.argument("color", StringArgumentType.word())
                    .suggests(COLOR_SUGGESTIONS)
                    .then(
                        Commands.argument("text", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val name = StringArgumentType.getString(ctx, "name")
                                val color = StringArgumentType.getString(ctx, "color")
                                val text = StringArgumentType.getString(ctx, "text")

                                PermissionManager.createPrefix(name, color, text)
                                TabHandler.updateAllPlayers(ctx.source.server)

                                val prefix = PermissionManager.storage.prefixes[name.lowercase()]

                                ctx.source.sendSuccess(
                                    {
                                        Component.literal(LanguageUtils.tr(ctx.source, "msg.create_prefix.success", name))
                                            .append(ColorUtils.parseColor(prefix?.text ?: ""))
                                    },
                                    true
                                )
                                1
                            }
                    )
            )

        rootNode.then(
            Commands.literal("создать")
                .requires { it.hasPermission(4) && isRussian(it) }
                .then(Commands.literal("группа").then(createGroupNode))
                .then(Commands.literal("префикс").then(createPrefixNode))
        )

        rootNode.then(
            Commands.literal("create")
                .requires { it.hasPermission(4) && isEnglish(it) }
                .then(Commands.literal("group").then(createGroupNode))
                .then(Commands.literal("prefix").then(createPrefixNode))
        )

        // 6. ИГРОК / USER (АДМИН - ВЫДАЧА И СБРОС)
        val userActionRu = Commands.argument("target", EntityArgument.player())
            .then(
                Commands.literal("группа")
                    .then(
                        Commands.argument("group_text", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val groupText = StringArgumentType.getString(ctx, "group_text")

                                PermissionManager.setPlayerGroup(target.uuid, groupText)
                                TabHandler.updatePlayerTab(target)

                                val formatted = ColorUtils.parseColor(groupText)
                                ctx.source.sendSuccess(
                                    {
                                        Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setgroup.success", target.name.string))
                                            .append(formatted)
                                    },
                                    true
                                )
                                1
                            }
                    )
            )
            .then(
                Commands.literal("префикс")
                    .then(
                        Commands.argument("prefix_text", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val prefixText = StringArgumentType.getString(ctx, "prefix_text")

                                PermissionManager.setPlayerPrefix(target.uuid, prefixText)
                                TabHandler.updatePlayerTab(target)

                                val formatted = ColorUtils.parseColor(prefixText)
                                ctx.source.sendSuccess(
                                    {
                                        Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setprefix.success", target.name.string))
                                            .append(formatted)
                                    },
                                    true
                                )
                                1
                            }
                    )
            )
            .then(
                Commands.literal("сброситьгруппу")
                    .executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PermissionManager.removePlayerGroup(target.uuid)
                        TabHandler.updatePlayerTab(target)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.removegroup.success", target.name.string)) }, true)
                        1
                    }
            )
            .then(
                Commands.literal("сброситьпрефикс")
                    .executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PermissionManager.removePlayerPrefix(target.uuid)
                        TabHandler.updatePlayerTab(target)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.removeprefix.success", target.name.string)) }, true)
                        1
                    }
            )

        val userActionEn = Commands.argument("target", EntityArgument.player())
            .then(
                Commands.literal("setgroup")
                    .then(
                        Commands.argument("group_text", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val groupText = StringArgumentType.getString(ctx, "group_text")

                                PermissionManager.setPlayerGroup(target.uuid, groupText)
                                TabHandler.updatePlayerTab(target)

                                val formatted = ColorUtils.parseColor(groupText)
                                ctx.source.sendSuccess(
                                    {
                                        Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setgroup.success", target.name.string))
                                            .append(formatted)
                                    },
                                    true
                                )
                                1
                            }
                    )
            )
            .then(
                Commands.literal("setprefix")
                    .then(
                        Commands.argument("prefix_text", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val prefixText = StringArgumentType.getString(ctx, "prefix_text")

                                PermissionManager.setPlayerPrefix(target.uuid, prefixText)
                                TabHandler.updatePlayerTab(target)

                                val formatted = ColorUtils.parseColor(prefixText)
                                ctx.source.sendSuccess(
                                    {
                                        Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setprefix.success", target.name.string))
                                            .append(formatted)
                                    },
                                    true
                                )
                                1
                            }
                    )
            )
            .then(
                Commands.literal("removegroup")
                    .executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PermissionManager.removePlayerGroup(target.uuid)
                        TabHandler.updatePlayerTab(target)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.removegroup.success", target.name.string)) }, true)
                        1
                    }
            )
            .then(
                Commands.literal("removeprefix")
                    .executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PermissionManager.removePlayerPrefix(target.uuid)
                        TabHandler.updatePlayerTab(target)
                        ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.removeprefix.success", target.name.string)) }, true)
                        1
                    }
            )

        rootNode.then(Commands.literal("игрок").requires { it.hasPermission(4) && isRussian(it) }.then(userActionRu))
        rootNode.then(Commands.literal("user").requires { it.hasPermission(4) && isEnglish(it) }.then(userActionEn))

        // 7. ИМЯ СЕРВЕРА / SERVERNAME (АДМИН)
        val serverNameNode = Commands.argument("text", StringArgumentType.greedyString())
            .executes { ctx ->
                val text = StringArgumentType.getString(ctx, "text")
                PermissionManager.setServerName(text)
                TabHandler.updateAllPlayers(ctx.source.server)

                val formatted = ColorUtils.parseColor(text)
                ctx.source.sendSuccess(
                    {
                        Component.literal(LanguageUtils.tr(ctx.source, "msg.servername.success"))
                            .append(formatted)
                    },
                    true
                )
                1
            }

        rootNode.then(Commands.literal("имясервера").requires { it.hasPermission(4) && isRussian(it) }.then(serverNameNode))
        rootNode.then(Commands.literal("servername").requires { it.hasPermission(4) && isEnglish(it) }.then(serverNameNode))

        dispatcher.register(rootNode)
    }

    private fun sendHelp(source: CommandSourceStack, type: String) {
        when (type) {
            "player", "игрок" -> {
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.header.player")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.join")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.leave")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.display")) }, false)
            }
            "admin", "админ" -> {
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.header.admin")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.create_group")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.create_prefix")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_setgroup")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_setprefix")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_removegroup")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_removeprefix")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.servername")) }, false)
            }
            else -> {
                source.sendFailure(Component.literal(LanguageUtils.tr(source, "help.invalid_type")))
            }
        }
    }
}