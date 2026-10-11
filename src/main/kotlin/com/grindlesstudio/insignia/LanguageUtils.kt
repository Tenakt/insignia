package com.grindlesstudio.insignia

import net.minecraft.commands.CommandSourceStack

object LanguageUtils {

    private val ru = mapOf(
        "help.header.player" to "§a=== Команды Insignia (Игрок) ===",
        "help.header.admin" to "§c=== Команды Insignia (Админ) ===",
        "help.cmd.join" to "§e/insignia вступить <группа> §7- Вступить в группу",
        "help.cmd.display" to "§e/insignia показ <чат|таб> <префикс|группа|выкл> §7- Выбрать режим отображения",
        "help.cmd.create_group" to "§e/insignia создать группа <название> <цвет> §7- Создать группу",
        "help.cmd.create_prefix" to "§e/insignia создать префикс <название> <цвет> <текст> §7- Создать префикс",
        "help.cmd.user_setgroup" to "§e/insignia игрок <игрок> группа <группа> §7- Выдать группу",
        "help.cmd.user_setprefix" to "§e/insignia игрок <игрок> префикс <префикс> §7- Выдать префикс",
        "help.invalid_type" to "§cИспользуйте: /insignia справка <игрок|админ>",

        "msg.join.success" to "§a[Insignia] Вы вступили в группу '%s'",
        "msg.join.fail" to "§c[Insignia] Группа '%s' не найдена!",
        "msg.display.success" to "§a[Insignia] В слоте '%s' теперь отображается: %s",
        "msg.display.invalid" to "§cДоступные режимы: префикс (prefix), группа (group), выкл (none)",

        "msg.create_group.success" to "§a[Insignia] Группа '%s' создана: ",
        "msg.create_prefix.success" to "§a[Insignia] Префикс '%s' создан: ",

        "msg.user.setgroup.success" to "§a[Insignia] Группа '%s' выдана игроку %s",
        "msg.user.setgroup.fail" to "§c[Insignia] Группа не найдена!",
        "msg.user.setprefix.success" to "§a[Insignia] Префикс '%s' выдан игроку %s",
        "msg.user.setprefix.fail" to "§c[Insignia] Префикс не найден!",

        "slot.chat" to "Чат",
        "slot.tab" to "Таб",
        "mode.prefix" to "Префикс",
        "mode.group" to "Группа",
        "mode.none" to "Выключено"
    )

    private val en = mapOf(
        "help.header.player" to "§a=== Insignia Commands (Player) ===",
        "help.header.admin" to "§c=== Insignia Commands (Admin) ===",
        "help.cmd.join" to "§e/insignia join <group> §7- Join a group",
        "help.cmd.display" to "§e/insignia display <chat|tab> <prefix|group|none> §7- Set display mode",
        "help.cmd.create_group" to "§e/insignia create group <name> <color> §7- Create a group",
        "help.cmd.create_prefix" to "§e/insignia create prefix <name> <color> <text> §7- Create a prefix",
        "help.cmd.user_setgroup" to "§e/insignia user <player> setgroup <group> §7- Assign group to player",
        "help.cmd.user_setprefix" to "§e/insignia user <player> setprefix <prefix> §7- Assign prefix to player",
        "help.invalid_type" to "§cUse: /insignia help <player|admin>",

        "msg.join.success" to "§a[Insignia] You joined group '%s'",
        "msg.join.fail" to "§c[Insignia] Group '%s' not found!",
        "msg.display.success" to "§a[Insignia] '%s' slot now displays: %s",
        "msg.display.invalid" to "§cAvailable modes: prefix, group, none",

        "msg.create_group.success" to "§a[Insignia] Group '%s' created: ",
        "msg.create_prefix.success" to "§a[Insignia] Prefix '%s' created: ",

        "msg.user.setgroup.success" to "§a[Insignia] Group '%s' assigned to %s",
        "msg.user.setgroup.fail" to "§c[Insignia] Group not found!",
        "msg.user.setprefix.success" to "§a[Insignia] Prefix '%s' assigned to %s",
        "msg.user.setprefix.fail" to "§c[Insignia] Prefix not found!",

        "slot.chat" to "Chat",
        "slot.tab" to "Tab",
        "mode.prefix" to "Prefix",
        "mode.group" to "Group",
        "mode.none" to "None"
    )

    fun getLanguage(source: CommandSourceStack): String {
        val player = source.player ?: return "ru_ru"
        return try {
            player.clientInformation().language().lowercase()
        } catch (e: Exception) {
            "ru_ru"
        }
    }

    fun tr(source: CommandSourceStack, key: String, vararg args: Any): String {
        val lang = getLanguage(source)
        val dict = if (lang.startsWith("ru")) ru else en
        val template = dict[key] ?: en[key] ?: key
        return if (args.isNotEmpty()) String.format(template, *args) else template
    }

    fun getSlotName(source: CommandSourceStack, slot: String): String {
        val normalized = when (slot.lowercase()) {
            "чат", "chat" -> "chat"
            "таб", "tab" -> "tab"
            else -> slot.lowercase()
        }
        return tr(source, "slot.$normalized")
    }

    fun getModeName(source: CommandSourceStack, mode: String): String {
        val normalized = when (mode.lowercase()) {
            "префикс", "prefix" -> "prefix"
            "группа", "group" -> "group"
            "выкл", "none", "ничего" -> "none"
            else -> mode.lowercase()
        }
        return tr(source, "mode.$normalized")
    }
}