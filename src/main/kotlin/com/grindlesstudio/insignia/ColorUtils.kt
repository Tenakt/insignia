package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ColorUtils {

    // Список доступных цветов и их соответствие кодам Minecraft
    val COLOR_MAP = mapOf(
        "red" to "&c",
        "dark_red" to "&4",
        "green" to "&a",
        "dark_green" to "&2",
        "blue" to "&9",
        "dark_blue" to "&1",
        "gold" to "&6",
        "yellow" to "&e",
        "aqua" to "&b",
        "dark_aqua" to "&3",
        "purple" to "&d",
        "dark_purple" to "&5",
        "gray" to "&7",
        "dark_gray" to "&8",
        "white" to "&f",
        "black" to "&0"
    )

    fun parseColor(text: String): MutableComponent {
        var formatted = text

        // Поддержка замены слов-цветов, если они есть в тексте
        COLOR_MAP.forEach { (name, code) ->
            formatted = formatted.replace(Regex("(?i)\\b$name\\b"), code)
        }

        // Преобразование знаков & в цвета Minecraft
        formatted = formatted.replace("&", "§")

        return Component.literal(formatted)
    }

    // Вспомогательный метод для получения цветового кода по слову
    fun getColorCode(colorName: String): String {
        return COLOR_MAP[colorName.lowercase()] ?: if (colorName.startsWith("&")) colorName else "&f"
    }
}