# Insignia

**Insignia** is a lightweight server-focused moderation and presentation utility for Minecraft 1.21.1 (NeoForge). It lets server owners organize players with groups and custom prefixes, and control how those tags appear in chat and the player list.

## Features

- **Groups and prefixes** — create reusable group and prefix templates.
- **Player-specific tags** — assign a template or a custom group/prefix to an individual player.
- **Independent display settings** — choose what appears in chat and in the player list separately.
- **Player control** — let players join available groups, remove their own tags, and choose their preferred display mode.
- **Custom player-list header** — set a server title and show server/player information.
- **Bilingual commands** — Russian and English command trees are selected using the command user's client language.
- **Persistent data** — groups, prefixes, player settings, and the server title are stored in `config/insignia.json`.

## Compatibility

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.256 |
| Java | 21 |
| KotlinForForge | 5.12.0 |

Insignia is built for NeoForge. It is not a Fabric or Forge build.

## Installation

1. Install Minecraft 1.21.1 with NeoForge 21.1.256.
2. Install the required **Kotlin for Forge / NeoForge** dependency (5.12.0 or a compatible version).
3. Put the Insignia JAR and its required dependency into your `mods` folder.
4. Start the server. Insignia creates `config/insignia.json` on first launch.

**Server note:** Insignia's features are implemented on the server. Players connect using the normal Minecraft client for the server's modded environment; install the required mod dependencies wherever NeoForge requires them.

## Commands

Commands are localized according to the language of the player executing them. The examples below use English command names. Use `/insignia help` or `/insignia help admin) for in-game help.

### Player commands

| Command | Description |
|---|---|
| `/insignia join <group>` | Join an existing group |
| `/insignia leave group` | Remove your group |
| `/insignia leave prefix` | Remove your prefix |
| `/insignia display chat prefix` | Show your prefix in chat |
| `/insignia display chat group` | Show your group in chat |
| `/insignia display chat none` | Hide your tag in chat |
| `/insignia display tab prefix` | Show your prefix in the player list |
| `/insignia display tab group` | Show your group in the player list |
| `/insignia display tab none` | Hide your tag in the player list |
| `/insignia help` | Show player help |

### Administrator commands

Administrative commands require **operator permission level 4**.

| Command | Description |
|---|---|
| `/insignia create group <name> <color>` | Create a group template |
| `/insignia create prefix <name> <color> <text>` | Create a prefix template |
| `/insignia user <player> setgroup <text>` | Assign a group template or custom group text |
| `/insignia user <player> setprefix <text>` | Assign a prefix template or custom prefix text |
| `/insignia user <player> removegroup` | Remove a player's group |
| `/insignia user <player> removeprefix` | Remove a player's prefix |
| `/insignia servername <text>` | Change the player-list server title |
| `/insignia help admin` | Show administrator help |

### Colors and formatting

Group and prefix templates accept color names in English or Russian, as well as Minecraft formatting codes.

Examples: `red`, `blue`, `green`, `красный`, `синий`, `зелёный`, `&c`, `&9`, `&a`.

Standard Minecraft color and text-formatting codes are supported.

## Configuration and data

Insignia stores its data in:

```text
config/insignia.json
```

The file contains group templates, prefix templates, player assignments/display preferences, and the player-list server title. Stop the server and make a backup before editing it manually.

## Building from source

Requirements: JDK 21.

Windows:

```powershell
.\gradlew.bat build
```

Linux/macOS:

```bash
./gradlew build
```

The compiled JAR is generated in `build/libs`.

## License

Insignia is distributed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE).
