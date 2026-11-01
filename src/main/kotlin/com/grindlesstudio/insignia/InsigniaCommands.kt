package com.grindlesstudio.insignia

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterCommandsEvent
import java.util.Locale

@EventBusSubscriber(modid = Insignia.MOD_ID)
object InsigniaCommands {

    private val COLOR_SUGGESTIONS =
        SuggestionProvider<CommandSourceStack> { _, builder ->
            SharedSuggestionProvider.suggest(
                ColorUtils.COLOR_MAP.keys,
                builder
            )
        }

    private val GROUP_SUGGESTIONS =
        SuggestionProvider<CommandSourceStack> { _, builder ->
            SharedSuggestionProvider.suggest(
                PermissionManager.storage.groups.keys,
                builder
            )
        }

    private val PREFIX_SUGGESTIONS =
        SuggestionProvider<CommandSourceStack> { _, builder ->
            SharedSuggestionProvider.suggest(
                PermissionManager.storage.prefixes.keys,
                builder
            )
        }

    private fun isRussian(source: CommandSourceStack): Boolean {
        return LanguageUtils.getLanguage(source).startsWith("ru")
    }

    private fun isEnglish(source: CommandSourceStack): Boolean {
        return !isRussian(source)
    }

    private fun message(
        source: CommandSourceStack,
        key: String,
        vararg args: Any
    ): MutableComponent {
        return Component.literal(LanguageUtils.tr(source, key, *args))
    }

    private fun executeDisplay(
        source: CommandSourceStack,
        slot: String,
        mode: String
    ): Int {
        val player = source.playerOrException

        if (!PermissionManager.setDisplayMode(player.uuid, slot, mode)) {
            source.sendFailure(message(source, "msg.display.invalid"))
            return 0
        }

        TabHandler.updatePlayerTab(player)

        source.sendSuccess(
            {
                message(
                    source,
                    "msg.display.success",
                    LanguageUtils.getSlotName(source, slot),
                    LanguageUtils.getModeName(source, mode)
                )
            },
            false
        )

        return 1
    }

    @JvmStatic
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        val root = Commands.literal("insignia")

        // Справка
        root.then(
            Commands.literal("справка")
                .requires(::isRussian)
                .executes {
                    sendHelp(it.source, "игрок")
                    1
                }
                .then(
                    Commands.literal("игрок").executes {
                        sendHelp(it.source, "игрок")
                        1
                    }
                )
                .then(
                    Commands.literal("админ").executes {
                        sendHelp(it.source, "админ")
                        1
                    }
                )
        )

        root.then(
            Commands.literal("help")
                .requires(::isEnglish)
                .executes {
                    sendHelp(it.source, "player")
                    1
                }
                .then(
                    Commands.literal("player").executes {
                        sendHelp(it.source, "player")
                        1
                    }
                )
                .then(
                    Commands.literal("admin").executes {
                        sendHelp(it.source, "admin")
                        1
                    }
                )
        )

        // Вступление в группу
        val joinAction = Commands.argument(
            "group_name",
            StringArgumentType.word()
        )
            .suggests(GROUP_SUGGESTIONS)
            .executes { ctx ->
                val player = ctx.source.playerOrException
                val name = StringArgumentType.getString(ctx, "group_name")
                    .lowercase(Locale.ROOT)

                if (name !in PermissionManager.storage.groups) {
                    ctx.source.sendFailure(
                        message(ctx.source, "msg.join.fail", name)
                    )
                    return@executes 0
                }

                PermissionManager.setPlayerGroup(player.uuid, name)
                TabHandler.updatePlayerTab(player)

                ctx.source.sendSuccess(
                    {
                        message(ctx.source, "msg.join.success", name)
                    },
                    false
                )

                1
            }

        root.then(
            Commands.literal("вступить")
                .requires(::isRussian)
                .then(joinAction)
        )

        root.then(
            Commands.literal("join")
                .requires(::isEnglish)
                .then(joinAction)
        )

        // Выход из группы и сброс префикса
        fun leaveCommand(
            rootName: String,
            groupName: String,
            prefixName: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(rootName)
                    .requires(languageCheck)
                    .then(
                        Commands.literal(groupName).executes { ctx ->
                            val player = ctx.source.playerOrException

                            PermissionManager.removePlayerGroup(player.uuid)
                            TabHandler.updatePlayerTab(player)

                            ctx.source.sendSuccess(
                                {
                                    message(
                                        ctx.source,
                                        "msg.leave.group.success"
                                    )
                                },
                                false
                            )

                            1
                        }
                    )
                    .then(
                        Commands.literal(prefixName).executes { ctx ->
                            val player = ctx.source.playerOrException

                            PermissionManager.removePlayerPrefix(player.uuid)
                            TabHandler.updatePlayerTab(player)

                            ctx.source.sendSuccess(
                                {
                                    message(
                                        ctx.source,
                                        "msg.leave.prefix.success"
                                    )
                                },
                                false
                            )

                            1
                        }
                    )
            )
        }

        leaveCommand("выйти", "группа", "префикс", ::isRussian)
        leaveCommand("leave", "group", "prefix", ::isEnglish)

        // Настройка отображения
        fun displayCommand(
            rootName: String,
            chatName: String,
            tabName: String,
            prefixName: String,
            groupName: String,
            noneName: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(rootName)
                    .requires(languageCheck)
                    .then(
                        Commands.literal(chatName)
                            .then(
                                Commands.literal(prefixName).executes {
                                    executeDisplay(
                                        it.source,
                                        "chat",
                                        "prefix"
                                    )
                                }
                            )
                            .then(
                                Commands.literal(groupName).executes {
                                    executeDisplay(
                                        it.source,
                                        "chat",
                                        "group"
                                    )
                                }
                            )
                            .then(
                                Commands.literal(noneName).executes {
                                    executeDisplay(
                                        it.source,
                                        "chat",
                                        "none"
                                    )
                                }
                            )
                    )
                    .then(
                        Commands.literal(tabName)
                            .then(
                                Commands.literal(prefixName).executes {
                                    executeDisplay(
                                        it.source,
                                        "tab",
                                        "prefix"
                                    )
                                }
                            )
                            .then(
                                Commands.literal(groupName).executes {
                                    executeDisplay(
                                        it.source,
                                        "tab",
                                        "group"
                                    )
                                }
                            )
                            .then(
                                Commands.literal(noneName).executes {
                                    executeDisplay(
                                        it.source,
                                        "tab",
                                        "none"
                                    )
                                }
                            )
                    )
            )
        }

        displayCommand(
            "показ", "чат", "таб",
            "префикс", "группа", "выкл", ::isRussian
        )

        displayCommand(
            "display", "chat", "tab",
            "prefix", "group", "none", ::isEnglish
        )

        // Создание группы
        fun createGroupCommand(
            commandName: String,
            groupWord: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(commandName)
                    .requires {
                        it.hasPermission(4) && languageCheck(it)
                    }
                    .then(
                        Commands.literal(groupWord)
                            .then(
                                Commands.argument(
                                    "name",
                                    StringArgumentType.word()
                                )
                                    .then(
                                        Commands.argument(
                                            "color",
                                            StringArgumentType.word()
                                        )
                                            .suggests(COLOR_SUGGESTIONS)
                                            .executes { ctx ->
                                                val name =
                                                    StringArgumentType.getString(
                                                        ctx,
                                                        "name"
                                                    ).trim()

                                                val color =
                                                    StringArgumentType.getString(
                                                        ctx,
                                                        "color"
                                                    )

                                                if (name.isBlank()) {
                                                    ctx.source.sendFailure(
                                                        Component.literal(
                                                            "Group name cannot be empty."
                                                        )
                                                    )
                                                    return@executes 0
                                                }

                                                val key = name.lowercase(Locale.ROOT)

                                                if (key in PermissionManager.storage.groups) {
                                                    ctx.source.sendFailure(
                                                        Component.literal(
                                                            "Group '$name' already exists."
                                                        )
                                                    )
                                                    return@executes 0
                                                }

                                                PermissionManager.createGroup(
                                                    name,
                                                    color
                                                )

                                                TabHandler.updateAllPlayers(
                                                    ctx.source.server
                                                )

                                                val group =
                                                    PermissionManager.storage.groups[key]

                                                ctx.source.sendSuccess(
                                                    {
                                                        message(
                                                            ctx.source,
                                                            "msg.create_group.success",
                                                            name
                                                        ).append(
                                                            ColorUtils.parseColor(
                                                                group?.text ?: ""
                                                            )
                                                        )
                                                    },
                                                    true
                                                )

                                                1
                                            }
                                    )
                            )
                    )
            )
        }

        createGroupCommand("создать", "группа", ::isRussian)
        createGroupCommand("create", "group", ::isEnglish)

        // Создание префикса
        fun createPrefixCommand(
            commandName: String,
            prefixWord: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(commandName)
                    .requires {
                        it.hasPermission(4) && languageCheck(it)
                    }
                    .then(
                        Commands.literal(prefixWord)
                            .then(
                                Commands.argument(
                                    "name",
                                    StringArgumentType.word()
                                )
                                    .then(
                                        Commands.argument(
                                            "color",
                                            StringArgumentType.word()
                                        )
                                            .suggests(COLOR_SUGGESTIONS)
                                            .then(
                                                Commands.argument(
                                                    "text",
                                                    StringArgumentType.greedyString()
                                                )
                                                    .executes { ctx ->
                                                        val name =
                                                            StringArgumentType.getString(
                                                                ctx,
                                                                "name"
                                                            ).trim()

                                                        val color =
                                                            StringArgumentType.getString(
                                                                ctx,
                                                                "color"
                                                            )

                                                        val text =
                                                            StringArgumentType.getString(
                                                                ctx,
                                                                "text"
                                                            ).trim()

                                                        if (name.isBlank() || text.isBlank()) {
                                                            ctx.source.sendFailure(
                                                                Component.literal(
                                                                    "Prefix name and text cannot be empty."
                                                                )
                                                            )
                                                            return@executes 0
                                                        }

                                                        val key = name.lowercase(Locale.ROOT)

                                                        if (key in PermissionManager.storage.prefixes) {
                                                            ctx.source.sendFailure(
                                                                Component.literal(
                                                                    "Prefix '$name' already exists."
                                                                )
                                                            )
                                                            return@executes 0
                                                        }

                                                        PermissionManager.createPrefix(
                                                            name,
                                                            color,
                                                            text
                                                        )

                                                        TabHandler.updateAllPlayers(
                                                            ctx.source.server
                                                        )

                                                        val prefix =
                                                            PermissionManager.storage.prefixes[key]

                                                        ctx.source.sendSuccess(
                                                            {
                                                                message(
                                                                    ctx.source,
                                                                    "msg.create_prefix.success",
                                                                    name
                                                                ).append(
                                                                    ColorUtils.parseColor(
                                                                        prefix?.text ?: ""
                                                                    )
                                                                )
                                                            },
                                                            true
                                                        )

                                                        1
                                                    }
                                            )
                                    )
                            )
                    )
            )
        }

        createPrefixCommand("создать", "префикс", ::isRussian)
        createPrefixCommand("create", "prefix", ::isEnglish)

        // Административные команды игрока
        fun userCommand(
            commandName: String,
            groupWord: String,
            prefixWord: String,
            removeGroupWord: String,
            removePrefixWord: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(commandName)
                    .requires {
                        it.hasPermission(4) && languageCheck(it)
                    }
                    .then(
                        Commands.argument(
                            "target",
                            EntityArgument.player()
                        )
                            .then(
                                Commands.literal(groupWord)
                                    .then(
                                        Commands.argument(
                                            "group_text",
                                            StringArgumentType.greedyString()
                                        )
                                            .suggests(GROUP_SUGGESTIONS)
                                            .executes { ctx ->
                                                val target =
                                                    EntityArgument.getPlayer(
                                                        ctx,
                                                        "target"
                                                    )

                                                val text =
                                                    StringArgumentType.getString(
                                                        ctx,
                                                        "group_text"
                                                    ).trim()

                                                if (text.isBlank()) {
                                                    return@executes 0
                                                }

                                                PermissionManager.setPlayerGroup(
                                                    target.uuid,
                                                    text
                                                )

                                                TabHandler.updatePlayerTab(target)

                                                ctx.source.sendSuccess(
                                                    {
                                                        message(
                                                            ctx.source,
                                                            "msg.user.setgroup.success",
                                                            target.name.string
                                                        ).append(
                                                            ColorUtils.parseColor(text)
                                                        )
                                                    },
                                                    true
                                                )

                                                1
                                            }
                                    )
                            )
                            .then(
                                Commands.literal(prefixWord)
                                    .then(
                                        Commands.argument(
                                            "prefix_text",
                                            StringArgumentType.greedyString()
                                        )
                                            .suggests(PREFIX_SUGGESTIONS)
                                            .executes { ctx ->
                                                val target =
                                                    EntityArgument.getPlayer(
                                                        ctx,
                                                        "target"
                                                    )

                                                val text =
                                                    StringArgumentType.getString(
                                                        ctx,
                                                        "prefix_text"
                                                    ).trim()

                                                if (text.isBlank()) {
                                                    return@executes 0
                                                }

                                                PermissionManager.setPlayerPrefix(
                                                    target.uuid,
                                                    text
                                                )

                                                TabHandler.updatePlayerTab(target)

                                                ctx.source.sendSuccess(
                                                    {
                                                        message(
                                                            ctx.source,
                                                            "msg.user.setprefix.success",
                                                            target.name.string
                                                        ).append(
                                                            ColorUtils.parseColor(text)
                                                        )
                                                    },
                                                    true
                                                )

                                                1
                                            }
                                    )
                            )
                            .then(
                                Commands.literal(removeGroupWord).executes { ctx ->
                                    val target =
                                        EntityArgument.getPlayer(ctx, "target")

                                    PermissionManager.removePlayerGroup(target.uuid)
                                    TabHandler.updatePlayerTab(target)

                                    ctx.source.sendSuccess(
                                        {
                                            message(
                                                ctx.source,
                                                "msg.user.removegroup.success",
                                                target.name.string
                                            )
                                        },
                                        true
                                    )

                                    1
                                }
                            )
                            .then(
                                Commands.literal(removePrefixWord).executes { ctx ->
                                    val target =
                                        EntityArgument.getPlayer(ctx, "target")

                                    PermissionManager.removePlayerPrefix(target.uuid)
                                    TabHandler.updatePlayerTab(target)

                                    ctx.source.sendSuccess(
                                        {
                                            message(
                                                ctx.source,
                                                "msg.user.removeprefix.success",
                                                target.name.string
                                            )
                                        },
                                        true
                                    )

                                    1
                                }
                            )
                    )
            )
        }

        userCommand(
            "игрок", "группа", "префикс",
            "сброситьгруппу", "сброситьпрефикс", ::isRussian
        )

        userCommand(
            "user", "setgroup", "setprefix",
            "removegroup", "removeprefix", ::isEnglish
        )

        // Название сервера
        fun serverNameCommand(
            commandName: String,
            languageCheck: (CommandSourceStack) -> Boolean
        ) {
            root.then(
                Commands.literal(commandName)
                    .requires {
                        it.hasPermission(4) && languageCheck(it)
                    }
                    .then(
                        Commands.argument(
                            "text",
                            StringArgumentType.greedyString()
                        )
                            .executes { ctx ->
                                val text =
                                    StringArgumentType.getString(
                                        ctx,
                                        "text"
                                    ).trim()

                                if (text.isBlank()) {
                                    ctx.source.sendFailure(
                                        Component.literal(
                                            "Server name cannot be empty."
                                        )
                                    )
                                    return@executes 0
                                }

                                PermissionManager.setServerName(text)
                                TabHandler.updateAllPlayers(ctx.source.server)

                                ctx.source.sendSuccess(
                                    {
                                        message(
                                            ctx.source,
                                            "msg.servername.success"
                                        ).append(ColorUtils.parseColor(text))
                                    },
                                    true
                                )

                                1
                            }
                    )
            )
        }

        serverNameCommand("имясервера", ::isRussian)
        serverNameCommand("servername", ::isEnglish)

        event.dispatcher.register(root)
    }

    private fun sendHelp(
        source: CommandSourceStack,
        type: String
    ) {
        when (type) {
            "player", "игрок" -> {
                source.sendSuccess(
                    { message(source, "help.header.player") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.join") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.leave") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.display") },
                    false
                )
            }

            "admin", "админ" -> {
                source.sendSuccess(
                    { message(source, "help.header.admin") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.create_group") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.create_prefix") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.user_setgroup") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.user_setprefix") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.user_removegroup") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.user_removeprefix") },
                    false
                )
                source.sendSuccess(
                    { message(source, "help.cmd.servername") },
                    false
                )
            }

            else -> {
                source.sendFailure(
                    message(source, "help.invalid_type")
                )
            }
        }
    }
}