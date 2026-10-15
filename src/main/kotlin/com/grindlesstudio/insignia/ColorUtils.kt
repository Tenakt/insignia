package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ColorUtils {

    // Словарь названий цветов (Русский + English -> Код Minecraft)
    val COLOR_MAP: Map<String, String> = mapOf(
        // Русский язык
        "черный" to "&0", "чёрный" to "&0",
        "темно_синий" to "&1", "темносиний" to "&1",
        "темно_зеленый" to "&2", "темнозеленый" to "&2",
        "бирюзовый" to "&3",
        "темно_красный" to "&4", "темнокрасный" to "&4",
        "фиолетовый" to "&5", "пурпурный" to "&5",
        "золотой" to "&6", "оранжевый" to "&6",
        "серый" to "&7",
        "темно_серый" to "&8", "темносерый" to "&8",
        "синий" to "&9",
        "зеленый" to "&a", "зелёный" to "&a",
        "голубой" to "&b",
        "красный" to "&c",
        "розовый" to "&d",
        "желтый" to "&e", "жёлтый" to "&e",
        "белый" to "&f",

        // English
        "black" to "&0",
        "dark_blue" to "&1",
        "dark_green" to "&2",
        "dark_aqua" to "&3",
        "dark_red" to "&4",
        "dark_purple" to "&5",
        "gold" to "&6",
        "gray" to "&7", "grey" to "&7",
        "dark_gray" to "&8",
        "blue" to "&9",
        "green" to "&a",
        "aqua" to "&b",
        "red" to "&c",
        "pink" to "&d",
        "yellow" to "&e",
        "white" to "&f"
    )

    /**
     * Преобразует ввод (слово "красный", "red" или код "&c") в формат "&X".
     */
    fun getColorCode(input: String): String {
        val cleanInput = input.lowercase().trim()

        // 1. Поиск по словарю названий
        COLOR_MAP[cleanInput]?.let { return it }

        // 2. Если уже передан код вида &a или §a
        if (cleanInput.startsWith("&") || cleanInput.startsWith("§")) {
            return cleanInput
        }

        // 3. Цвет по умолчанию (белый)
        return "&f"
    }

    /**
     * Преобразует строку с символами & в форматированный Component
     */
    fun parseColor(text: String): MutableComponent {
        val formatted = text.replace("&", "§")
        return Component.literal(formatted)
    }
}