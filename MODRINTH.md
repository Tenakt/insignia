# Modrinth listing — Insignia

## Short description

A server-side group and prefix manager for Minecraft 1.21.1 NeoForge, with customizable player tags and separate chat and player-list display settings.

## Long description

**Organize your server's identity with Insignia.**

Insignia gives server owners a simple way to manage player groups and prefixes without needing a permissions suite. Create reusable tag templates, assign custom tags to individual players, and decide how each player's tag appears in chat and in the player list.

### Features

- **Group templates:** create named groups with custom colors.
- **Prefix templates:** create reusable prefixes with custom text and colors.
- **Custom player tags:** assign a saved template or custom text to any player.
- **Separate chat and tab settings:** players can choose a group, prefix, or no tag independently for chat and the player list.
- **Player self-service:** players can join existing groups, remove their own group or prefix, and configure their display preferences.
- **Custom player-list header:** set a server title and display useful server and player information.
- **Russian and English commands:** command language follows the executing player's client language.
- **Persistent storage:** settings are saved in `config/insignia.json`.

### Requirements

- Minecraft **1.21.1**
- **NeoForge 21.1.256**
- **Java 21**
- **Kotlin for Forge / NeoForge 5.12.0** (required)

### Installation

1. Install NeoForge 21.1.256 for Minecraft 1.21.1 on your server.
2. Add Insignia and its required Kotlin for Forge / NeoForge dependency to the server's `mods` folder.
3. Start the server. Insignia creates its configuration file automatically.

Insignia is designed to run on the server. Players do not need to install Insignia on their clients to join a server running it.

### Player commands

English command names are shown below. Russian command equivalents are available when the executing player uses a Russian client language.

| Command | What it does |
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

All administrator commands require **operator permission level 4**.

| Command | What it does |
|---|---|
| `/insignia create group <name> <color>` | Create a group template |
| `/insignia create prefix <name> <color> <text>` | Create a prefix template |
| `/insignia user <player> setgroup <text>` | Assign a group template or custom group text |
| `/insignia user <player> setprefix <text>` | Assign a prefix template or custom prefix text |
| `/insignia user <player> removegroup` | Remove a player's group |
| `/insignia user <player> removeprefix` | Remove a player's prefix |
| `/insignia servername <text>` | Change the player-list server title |
| `/insignia help admin` | Show administrator help |

### Colors

Use color names in English or Russian, or Minecraft formatting codes such as `&c`, `&9`, and `&a`. Standard Minecraft color and text-formatting codes are supported.

### Configuration

All Insignia data is stored in `config/insignia.json`. Stop the server and make a backup before editing this file manually.

### License

GNU General Public License v3.0 (GPL-3.0-only).
