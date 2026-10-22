package com.grindlesstudio.insignia

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.Locale

object ColorUtils {

    /**
     * Названия цветов на русском и английском языках.
     */
    val COLOR_MAP: Map<String, String> = mapOf(
        // Русский
        "черный" to "&0",
        "чёрный" to "&0",
        "темно_синий" to "&1",
        "темносиний" to "&1",
        "темно_зеленый" to "&2",
        "темнозеленый" to "&2",
        "бирюзовый" to "&3",
        "темно_красный" to "&4",
        "темнокрасный" to "&4",
        "фиолетовый" to "&5",
        "пурпурный" to "&5",
        "золотой" to "&6",
        "оранжевый" to "&6",
        "серый" to "&7",
        "темно_серый" to "&8",
        "темносерый" to "&8",
        "синий" to "&9",
        "зеленый" to "&a",
        "зелёный" to "&a",
        "голубой" to "&b",
        "красный" to "&c",
        "розовый" to "&d",
        "желтый" to "&e",
        "жёлтый" to "&e",
        "белый" to "&f",

        // English
        "black" to "&0",
        "dark_blue" to "&1",
        "dark_green" to "&2",
        "dark_aqua" to "&3",
        "dark_red" to "&4",
        "dark_purple" to "&5",
        "gold" to "&6",
        "gray" to "&7",
        "grey" to "&7",
        "dark_gray" to "&8",
        "dark_grey" to "&8",
        "blue" to "&9",
        "green" to "&a",
        "aqua" to "&b",
        "red" to "&c",
        "pink" to "&d",
        "yellow" to "&e",
        "white" to "&f"
    )

    /**
     * Допустимые стандартные коды Minecraft:
     * цвета 0-f и форматирование k-o, r.
     */
    private val validCodeRegex = Regex("^[&§][0-9a-fk-or]$", RegexOption.IGNORE_CASE)

    /**
     * Заменяет только допустимые коды форматирования.
     *
     * Например:
     * &cКрасный -> §cКрасный
     * &lЖирный -> §lЖирный
     * &zzТекст -> &zzТекст
     */
    private val formattingRegex = Regex("[&§]([0-9a-fk-or])", RegexOption.IGNORE_CASE)

    /**
     * Преобразует название цвета или код в код Minecraft.
     * При неизвестном значении возвращает белый цвет.
     */
    fun getColorCode(input: String): String {
        val value = input.trim().lowercase(Locale.ROOT)

        COLOR_MAP[value]?.let { return it }

        if (validCodeRegex.matches(value)) {
            return "&${value[1].lowercaseChar()}"
        }

        return "&f"
    }

    /**
     * Преобразует допустимые &-коды и §-коды в форматированный Component.
     */
    fun parseColor(text: String): MutableComponent {
        val formatted = formattingRegex.replace(text) { match ->
            "§${match.groupValues[1].lowercase(Locale.ROOT)}"
        }

        return Component.literal(formatted)
    }
}