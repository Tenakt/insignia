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

// Стандартно значения равны null — никаких автоматических префиксов
data class UserData(
    var groupName: String? = null,
    var prefixName: String? = null,
    var chatDisplay: DisplayMode = DisplayMode.PREFIX,
    var tabDisplay: DisplayMode = DisplayMode.GROUP
)

data class DataStorage(
    val groups: MutableMap<String, Group> = mutableMapOf(),
    val prefixes: MutableMap<String, Prefix> = mutableMapOf(),
    val players: MutableMap<String, UserData> = mutableMapOf()
)

object PermissionManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File = FMLPaths.CONFIGDIR.get().resolve("insignia.json").toFile()

    var storage = DataStorage()
        private set

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

    fun setPlayerGroup(uuid: UUID, groupName: String): Boolean {
        val key = groupName.lowercase()
        if (!storage.groups.containsKey(key)) return false
        getUserData(uuid).groupName = key
        save()
        return true
    }

    fun setPlayerPrefix(uuid: UUID, prefixName: String): Boolean {
        val key = prefixName.lowercase()
        if (!storage.prefixes.containsKey(key)) return false
        getUserData(uuid).prefixName = key
        save()
        return true
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

        return when (mode) {
            DisplayMode.PREFIX -> {
                val p = user.prefixName?.let { storage.prefixes[it] }
                p?.let { ColorUtils.parseColor("${it.text} ") } ?: Component.empty()
            }
            DisplayMode.GROUP -> {
                val g = user.groupName?.let { storage.groups[it] }
                g?.let { ColorUtils.parseColor("${it.text} ") } ?: Component.empty()
            }
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