# Currencies

## Description

Currencies is an expansion for [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) that allows faction owners to create and mint local currencies, which paves the way for the simulation of local economies.

## Supported Minecraft Versions
This plugin is supported on the Minecraft versions listed in [`minecraft-versions.json`](minecraft-versions.json): currently **1.19.4**, **1.21.11**, **26.2** and **26.3** (Spigot and its forks). Every stable release is booted on a real server of each of these versions before it is published, and every build checks that the plugin only uses Bukkit API that exists on all of them. Other versions from 1.19.4 onwards are expected to work but are not tested. To support another version, add it to the file: both checks pick it up.

## Installation

### First Time Installation

1. Download the plugin from [SpigotMC](https://www.spigotmc.org/resources/currencies.96381/).
2. Place the jar in the `plugins` folder of your server.
3. Restart your server.

### Dependencies

This plugin depends on [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) in order to work.

**Compatibility:** Currencies v3.0.0 was enabled against [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) 7.0.0 by the [dependents gate](https://github.com/Dans-Plugins/release-gates/actions/runs/36957984824) before Medieval Factions 7.0.0 was published.

**Other Medieval Factions expansions:** [Fiefs](https://github.com/Dans-Plugins/Fiefs) (sub-factions), [Democracy](https://github.com/Dans-Plugins/Democracy) (elections), [Bluemap_MedievalFactions](https://github.com/Dans-Plugins/Bluemap_MedievalFactions) (claims on a BlueMap web map). All of them are listed in the [Medieval Factions README](https://github.com/Dans-Plugins/Medieval-Factions#expansions).

Currencies 2.x does not enable on Medieval Factions 5.8 or newer. Use Currencies 3.0.0 or later.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all commands
- [Configuration Guide](CONFIG.md) – Detailed configuration options

### Wiki & Additional Resources

- [Wiki Guide](https://github.com/Dans-Plugins/Currencies/wiki/Guide)
- [FAQ](https://github.com/Dans-Plugins/Currencies/wiki/FAQ)

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/Currencies/issues/new).

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)
- [Notes for Developers](https://github.com/Dans-Plugins/Currencies/wiki/Developer-Notes)

## Testing

### Unit Tests

Linux:

    ./gradlew clean test

Windows:

    .\gradlew.bat clean test

If you see `BUILD SUCCESSFUL`, the tests have passed.

## Development

### Test Server with Plugin Hot-Reloading

A Docker-based test server is available for development.

#### Setup

1. Copy `sample.env` to `.env` and configure as needed.
2. Build the plugin: `./gradlew build`
3. Start the test server: `./up.sh`

#### Stopping the Test Server

    ./down.sh

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|---------------------|
| Daniel Stephenson | Creator |
| alyphen | Wrote Currencies 2 — the Kotlin rewrite, the jOOQ/Flyway database storage layer, and the coinpurse and currency item systems built on it |
| Deej | Added the FurnaceHandler |
| tdlotrring | Fixed a bug with minting costing power even upon failure |
| Rykurock | Corrected some usage messages and fixed some typos |

It was Ricortix's suggestion to create a plugin like this one.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Roadmap

- [Known Bugs](https://github.com/Dans-Plugins/Currencies/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
- [Planned Features](https://github.com/Dans-Plugins/Currencies/issues?q=is%3Aopen+is%3Aissue+label%3AEpic)
- [Planned Improvements](https://github.com/Dans-Plugins/Currencies/issues?q=is%3Aopen+is%3Aissue+label%3Aimprovement)

## Changelog

- [CHANGELOG.md](CHANGELOG.md)

## Project Status

This project is in active development.

### bStats

You can view the bStats page for the plugin [here](https://bstats.org/plugin/bukkit/Currencies/12810).

## Usage reporting

Usage reporting is on by default: when the plugin is enabled, and each time `/currency` or
`/coinpurse` is used, it sends its name, its version and the command's name (`startup` and `command`
events) to https://trace.danielstephenson.dev so it is known which plugins are actually in use.
Nothing about players, worlds, IPs, or anything typed after a command is sent. The plugin
says on every startup whether reporting is on. Each event also carries a random server ID (the `server-id` line in `plugins/trace/config.yml`) so
servers can be counted rather than events. It identifies no person, account or IP address; delete
the line to get a new one.

To turn it off:

- for this plugin: `usage-reporting.enabled: false` in `plugins/Currencies/config.yml`
- for every plugin on the server that reports this way: `enabled: false` in `plugins/trace/config.yml`
  (created the first time such a plugin starts)
- for the whole process: the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`

Details: https://danielstephenson.dev/usage-reporting
