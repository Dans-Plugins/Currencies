# Currencies

## Description

Currencies is an expansion for [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) that allows faction owners to create and mint local currencies, which paves the way for the simulation of local economies.

## Installation

### First Time Installation

1. Download the plugin from [SpigotMC](https://www.spigotmc.org/resources/currencies.96381/).
2. Place the jar in the `plugins` folder of your server.
3. Restart your server.

### Dependencies

This plugin depends on [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) in order to work.

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

- [Known Bugs](https://github.com/Dans-Plugins/Currencies/issues?q=is%3Aopen+is%3Aissue+label%3Abug)

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
