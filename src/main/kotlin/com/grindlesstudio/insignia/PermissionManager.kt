package com.grindlesstudio.insignia

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.neoforged.fml.loading.FMLPaths
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

enum class DisplayMode {
    PREFIX,
    GROUP,
    NONE
}

data class Group(
    val name: String,
    var text: String
)

data class Prefix(
    val name: String,
    var text: String
)

data class UserData(
    var groupName: String? = null,
    var prefixName: String? = null,
    var customPrefix: String? = null,
    var customGroup: String? = null,
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

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val configFile: File =
        FMLPaths.CONFIGDIR.get().resolve("insignia.json").toFile()

    var storage: DataStorage = DataStorage()
        private set

    /**
     * Изменяет название сервера в таб-листе.
     */
    fun setServerName(newName: String) {
        storage.serverName = newName
        save()
    }

    /**
     * Создаёт или обновляет шаблон группы.
     */
    fun createGroup(name: String, colorArg: String) {
        val key = name.trim().lowercase()
        if (key.isBlank()) return

        val colorCode = ColorUtils.getColorCode(colorArg)
        val formatted = "$colorCode[${name.trim()}]"

        storage.groups[key] = Group(
            name = key,
            text = formatted
        )

        save()
    }

    /**
     * Создаёт или обновляет шаблон префикса.
     */
    fun createPrefix(name: String, colorArg: String, rawText: String) {
        val key = name.trim().lowercase()
        if (key.isBlank()) return

        val colorCode = ColorUtils.getColorCode(colorArg)
        val formatted = "$colorCode$rawText"

        storage.prefixes[key] = Prefix(
            name = key,
            text = formatted
        )

        save()
    }

    /**
     * Возвращает данные игрока.
     */
    fun getUserData(uuid: UUID): UserData {
        val key = uuid.toString()

        return storage.players.getOrPut(key) {
            UserData()
        }
    }

    /**
     * Назначает группу из шаблонов либо личную группу.
     */
    fun setPlayerGroup(uuid: UUID, groupInput: String) {
        val value = groupInput.trim()
        if (value.isBlank()) return

        val key = value.lowercase()
        val user = getUserData(uuid)

        if (storage.groups.containsKey(key)) {
            user.groupName = key
            user.customGroup = null
        } else {
            user.groupName = null
            user.customGroup = value
        }

        save()
    }

    /**
     * Удаляет группу игрока.
     */
    fun removePlayerGroup(uuid: UUID) {
        val user = getUserData(uuid)

        user.groupName = null
        user.customGroup = null

        save()
    }

    /**
     * Назначает префикс из шаблонов либо личный префикс.
     */
    fun setPlayerPrefix(uuid: UUID, prefixInput: String) {
        val value = prefixInput.trim()
        if (value.isBlank()) return

        val key = value.lowercase()
        val user = getUserData(uuid)

        if (storage.prefixes.containsKey(key)) {
            user.prefixName = key
            user.customPrefix = null
        } else {
            user.prefixName = null
            user.customPrefix = value
        }

        save()
    }

    /**
     * Удаляет префикс игрока.
     */
    fun removePlayerPrefix(uuid: UUID) {
        val user = getUserData(uuid)

        user.prefixName = null
        user.customPrefix = null

        save()
    }

    /**
     * Настраивает отображение тегов в чате и таб-листе.
     *
     * Поддерживаемые слоты:
     * chat / чат
     * tab / таб
     *
     * Поддерживаемые режимы:
     * prefix / префикс
     * group / группа
     * none / выкл / ничего
     */
    fun setDisplayMode(
        uuid: UUID,
        slotRaw: String,
        modeRaw: String
    ): Boolean {
        val slot = when (slotRaw.trim().lowercase()) {
            "чат", "chat" -> "chat"
            "таб", "tab" -> "tab"
            else -> return false
        }

        val mode = when (modeRaw.trim().lowercase()) {
            "префикс", "prefix" -> DisplayMode.PREFIX
            "группа", "group" -> DisplayMode.GROUP
            "выкл", "none", "ничего" -> DisplayMode.NONE
            else -> return false
        }

        val data = getUserData(uuid)

        when (slot) {
            "chat" -> data.chatDisplay = mode
            "tab" -> data.tabDisplay = mode
        }

        save()
        return true
    }

    /**
     * Формирует тег игрока для чата или таб-листа.
     *
     * Сохраняет прежнее поведение: если выбранный тип тега
     * отсутствует, используется другой доступный тип.
     */
    fun getFormattedTag(
        uuid: UUID,
        isChat: Boolean
    ): MutableComponent {
        val user = getUserData(uuid)
        val mode = if (isChat) {
            user.chatDisplay
        } else {
            user.tabDisplay
        }

        if (mode == DisplayMode.NONE) {
            return Component.empty()
        }

        val customPrefixComponent = user.customPrefix
            ?.let { ColorUtils.parseColor("$it ") }

        val customGroupComponent = user.customGroup
            ?.let { ColorUtils.parseColor("$it ") }

        val prefixTemplate = user.prefixName
            ?.let { storage.prefixes[it] }

        val groupTemplate = user.groupName
            ?.let { storage.groups[it] }

        val prefixComponent = customPrefixComponent
            ?: prefixTemplate?.let {
                ColorUtils.parseColor("${it.text} ")
            }

        val groupComponent = customGroupComponent
            ?: groupTemplate?.let {
                ColorUtils.parseColor("${it.text} ")
            }

        return when (mode) {
            DisplayMode.PREFIX ->
                prefixComponent ?: groupComponent ?: Component.empty()

            DisplayMode.GROUP ->
                groupComponent ?: prefixComponent ?: Component.empty()

            DisplayMode.NONE -> Component.empty()
        }
    }

    /**
     * Сохраняет конфигурацию через временный файл.
     */
    @Synchronized
    fun save() {
        val parent = configFile.parentFile

        try {
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs() && !parent.isDirectory) {
                    throw IllegalStateException(
                        "Не удалось создать каталог конфигурации: $parent"
                    )
                }
            }

            val tempFile = File(parent, "${configFile.name}.tmp")

            try {
                tempFile.writeText(gson.toJson(storage))

                try {
                    Files.move(
                        tempFile.toPath(),
                        configFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: Exception) {
                    Files.move(
                        tempFile.toPath(),
                        configFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            System.err.println(
                "[Insignia] Не удалось сохранить конфигурацию: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    /**
     * Загружает конфигурацию.
     *
     * Отсутствующие поля заполняются значениями по умолчанию.
     * Ошибочные записи пропускаются отдельно.
     * При ошибке чтения исходный файл не перезаписывается.
     */
    @Synchronized
    fun load() {
        if (!configFile.exists()) {
            save()
            return
        }

        try {
            val root = JsonParser.parseString(
                configFile.readText()
            )

            if (!root.isJsonObject) {
                throw IllegalStateException(
                    "Корень insignia.json должен быть JSON-объектом"
                )
            }

            val json = root.asJsonObject
            val loaded = DataStorage()

            loaded.serverName = json.get("serverName")
                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                ?.asString
                ?: loaded.serverName

            loadGroups(json, loaded)
            loadPrefixes(json, loaded)
            loadPlayers(json, loaded)

            storage = loaded

            // Нормализуем файл после успешного чтения.
            // Сохранение выполняется только после полной загрузки.
            save()

            println(
                "[Insignia] Конфигурация загружена: " +
                        "${storage.groups.size} групп, " +
                        "${storage.prefixes.size} префиксов, " +
                        "${storage.players.size} игроков."
            )
        } catch (e: Exception) {
            System.err.println(
                "[Insignia] Не удалось загрузить insignia.json. " +
                        "Текущий файл оставлен без изменений: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    private fun loadGroups(
        json: JsonObject,
        target: DataStorage
    ) {
        val element = json.get("groups")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return

        for ((key, value) in element.entrySet()) {
            try {
                if (!value.isJsonObject) continue

                val group = gson.fromJson(
                    value,
                    Group::class.java
                ) ?: continue

                if (group.name.isBlank()) continue

                target.groups[key.lowercase()] = group
            } catch (e: Exception) {
                System.err.println(
                    "[Insignia] Пропущена повреждённая группа '$key': " +
                            e.message
                )
            }
        }
    }

    private fun loadPrefixes(
        json: JsonObject,
        target: DataStorage
    ) {
        val element = json.get("prefixes")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return

        for ((key, value) in element.entrySet()) {
            try {
                if (!value.isJsonObject) continue

                val prefix = gson.fromJson(
                    value,
                    Prefix::class.java
                ) ?: continue

                if (prefix.name.isBlank()) continue

                target.prefixes[key.lowercase()] = prefix
            } catch (e: Exception) {
                System.err.println(
                    "[Insignia] Пропущен повреждённый префикс '$key': " +
                            e.message
                )
            }
        }
    }

    private fun loadPlayers(
        json: JsonObject,
        target: DataStorage
    ) {
        val element = json.get("players")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: return

        for ((key, value) in element.entrySet()) {
            try {
                if (!value.isJsonObject) continue

                val player = gson.fromJson(
                    value,
                    UserData::class.java
                ) ?: continue

                // Проверяем ссылки на существующие шаблоны.
                if (player.groupName !in target.groups) {
                    player.groupName = null
                }

                if (player.prefixName !in target.prefixes) {
                    player.prefixName = null
                }

                target.players[key] = player
            } catch (e: Exception) {
                System.err.println(
                    "[Insignia] Пропущена повреждённая запись " +
                            "игрока '$key': ${e.message}"
                )
            }
        }
    }
}