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

    // Поддержка языка: если команду пишет консоль (source.player == null), разрешаем обе ветки
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
            player.refreshTabListName()
            player.refreshDisplayName()

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

        // ---------------------------------------------------------------------
        // 1. СПРАВКА / HELP
        // ---------------------------------------------------------------------
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

        // ---------------------------------------------------------------------
        // 2. ВСТУПИТЬ / JOIN
        // ---------------------------------------------------------------------
        val joinAction = Commands.argument("group_name", StringArgumentType.word())
            .executes { ctx ->
                val player = ctx.source.playerOrException
                val groupName = StringArgumentType.getString(ctx, "group_name")

                if (PermissionManager.setPlayerGroup(player.uuid, groupName)) {
                    player.refreshTabListName()
                    player.refreshDisplayName()
                    ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.join.success", groupName)) }, false)
                } else {
                    ctx.source.sendFailure(Component.literal(LanguageUtils.tr(ctx.source, "msg.join.fail", groupName)))
                }
                1
            }

        rootNode.then(Commands.literal("вступить").requires { isRussian(it) }.then(joinAction))
        rootNode.then(Commands.literal("join").requires { isEnglish(it) }.then(joinAction))

        // ---------------------------------------------------------------------
        // 3. ПОКАЗ / DISPLAY
        // ---------------------------------------------------------------------
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

        // ---------------------------------------------------------------------
        // 4. СОЗДАТЬ / CREATE (АДМИН)
        // ---------------------------------------------------------------------
        val createGroupNode = Commands.argument("name", StringArgumentType.word())
            .then(
                Commands.argument("color", StringArgumentType.word())
                    .suggests(COLOR_SUGGESTIONS)
                    .executes { ctx ->
                        val name = StringArgumentType.getString(ctx, "name")
                        val color = StringArgumentType.getString(ctx, "color")

                        PermissionManager.createGroup(name, color)
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

        // ---------------------------------------------------------------------
        // 5. ИГРОК / USER (АДМИН)
        // ---------------------------------------------------------------------
        val userAction = Commands.argument("target", EntityArgument.player())
            .then(
                Commands.literal("setgroup")
                    .then(
                        Commands.argument("group_name", StringArgumentType.word())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val groupName = StringArgumentType.getString(ctx, "group_name")

                                if (PermissionManager.setPlayerGroup(target.uuid, groupName)) {
                                    target.refreshTabListName()
                                    target.refreshDisplayName()
                                    ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setgroup.success", groupName, target.name.string)) }, true)
                                } else {
                                    ctx.source.sendFailure(Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setgroup.fail")))
                                }
                                1
                            }
                    )
            )
            .then(
                Commands.literal("setprefix")
                    .then(
                        Commands.argument("prefix_name", StringArgumentType.word())
                            .executes { ctx ->
                                val target = EntityArgument.getPlayer(ctx, "target")
                                val prefixName = StringArgumentType.getString(ctx, "prefix_name")

                                if (PermissionManager.setPlayerPrefix(target.uuid, prefixName)) {
                                    target.refreshTabListName()
                                    target.refreshDisplayName()
                                    ctx.source.sendSuccess({ Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setprefix.success", prefixName, target.name.string)) }, true)
                                } else {
                                    ctx.source.sendFailure(Component.literal(LanguageUtils.tr(ctx.source, "msg.user.setprefix.fail")))
                                }
                                1
                            }
                    )
            )

        rootNode.then(Commands.literal("игрок").requires { it.hasPermission(4) && isRussian(it) }.then(userAction))
        rootNode.then(Commands.literal("user").requires { it.hasPermission(4) && isEnglish(it) }.then(userAction))

        dispatcher.register(rootNode)
    }

    private fun sendHelp(source: CommandSourceStack, type: String) {
        when (type) {
            "player", "игрок" -> {
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.header.player")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.join")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.display")) }, false)
            }
            "admin", "админ" -> {
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.header.admin")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.create_group")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.create_prefix")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_setgroup")) }, false)
                source.sendSuccess({ Component.literal(LanguageUtils.tr(source, "help.cmd.user_setprefix")) }, false)
            }
            else -> {
                source.sendFailure(Component.literal(LanguageUtils.tr(source, "help.invalid_type")))
            }
        }
    }
}