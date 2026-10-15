package com.grindlesstudio.insignia

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.neoforged.fml.loading.FMLPaths
import java.io.File
import java.util.UUID

enum class DisplayMode {
    PREFIX, GROUP, NONE
}

data class Group(val name: String, var text: String)
data class Prefix(val name: String, var text: String)

data class UserData(
    var groupName: String? = null,
    var prefixName: String? = null,
    var customPrefix: String? = null, // Личный префикс игрока
    var customGroup: String? = null,   // Личный клан/группа игрока
    var chatDisplay: DisplayMode = DisplayMode.PREFIX,
    var tabDisplay: DisplayMode = DisplayMode.GROUP
)

data class DataStorage(
    val groups: MutableMap<String, Group> = mutableMapOf(),
    val prefixes: MutableMap<String, Prefix> = mutableMapOf(),
    val players: MutableMap<String, UserData> = mutableMapOf(),
    var serverName: String = "&b&lServer name"
)

object PermissionManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File = FMLPaths.CONFIGDIR.get().resolve("insignia.json").toFile()

    var storage = DataStorage()
        private set

    fun setServerName(newName: String) {
        storage.serverName = newName
        save()
    }

    fun createGroup(name: String, colorArg: String) {
        val lowerName = name.lowercase()
        val colorCode = ColorUtils.getColorCode(colorArg)
        val formatted = "$colorCode[$name]"
        storage.groups[lowerName] = Group(lowerName, formatted)
        save()
    }

    fun createPrefix(name: String, colorArg: String, rawText: String) {
        val lowerName = name.lowercase()
        val colorCode = ColorUtils.getColorCode(colorArg)
        val formatted = "$colorCode$rawText"
        storage.prefixes[lowerName] = Prefix(lowerName, formatted)
        save()
    }

    fun getUserData(uuid: UUID): UserData {
        return storage.players.getOrPut(uuid.toString()) { UserData() }
    }

    // Установка группы (если есть в базе — берем из базы, если нет — ставим как кастомный клан)
    fun setPlayerGroup(uuid: UUID, groupInput: String) {
        val key = groupInput.lowercase()
        val user = getUserData(uuid)

        if (storage.groups.containsKey(key)) {
            user.groupName = key
            user.customGroup = null
        } else {
            user.groupName = null
            user.customGroup = ColorUtils.getColorCode("") + groupInput
        }
        save()
    }

    fun removePlayerGroup(uuid: UUID) {
        val user = getUserData(uuid)
        user.groupName = null
        user.customGroup = null
        save()
    }

    // Установка префикса (если есть в базе — берем из базы, если нет — ставим как кастомный префикс)
    fun setPlayerPrefix(uuid: UUID, prefixInput: String) {
        val key = prefixInput.lowercase()
        val user = getUserData(uuid)

        if (storage.prefixes.containsKey(key)) {
            user.prefixName = key
            user.customPrefix = null
        } else {
            user.prefixName = null
            user.customPrefix = prefixInput
        }
        save()
    }

    fun removePlayerPrefix(uuid: UUID) {
        val user = getUserData(uuid)
        user.prefixName = null
        user.customPrefix = null
        save()
    }

    fun setDisplayMode(uuid: UUID, slotRaw: String, modeRaw: String): Boolean {
        val data = getUserData(uuid)
        val slot = when (slotRaw.lowercase()) {
            "чат", "chat" -> "chat"
            "таб", "tab" -> "tab"
            else -> return false
        }
        val mode = when (modeRaw.lowercase()) {
            "префикс", "prefix" -> DisplayMode.PREFIX
            "группа", "group" -> DisplayMode.GROUP
            "выкл", "none", "ничего" -> DisplayMode.NONE
            else -> return false
        }

        if (slot == "chat") data.chatDisplay = mode
        if (slot == "tab") data.tabDisplay = mode
        save()
        return true
    }

    fun getFormattedTag(uuid: UUID, isChat: Boolean): MutableComponent {
        val user = getUserData(uuid)
        val mode = if (isChat) user.chatDisplay else user.tabDisplay

        if (mode == DisplayMode.NONE) return Component.empty()

        // 1. Приоритет личных кастомных значений
        val customPrefixComp = user.customPrefix?.let { ColorUtils.parseColor("$it ") }
        val customGroupComp = user.customGroup?.let { ColorUtils.parseColor("$it ") }

        // 2. Значения из глобальных шаблонов
        val prefixObj = user.prefixName?.let { storage.prefixes[it] }
        val groupObj = user.groupName?.let { storage.groups[it] }

        val prefixComp = customPrefixComp ?: prefixObj?.let { ColorUtils.parseColor("${it.text} ") }
        val groupComp = customGroupComp ?: groupObj?.let { ColorUtils.parseColor("${it.text} ") }

        return when (mode) {
            DisplayMode.PREFIX -> prefixComp ?: groupComp ?: Component.empty()
            DisplayMode.GROUP -> groupComp ?: prefixComp ?: Component.empty()
            DisplayMode.NONE -> Component.empty()
        }
    }

    fun save() {
        try {
            configFile.writeText(gson.toJson(storage))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load() {
        if (!configFile.exists()) {
            save()
            return
        }
        try {
            val loaded = gson.fromJson(configFile.readText(), DataStorage::class.java)
            if (loaded != null) storage = loaded
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}