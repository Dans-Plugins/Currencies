# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get currencies --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

## [3.0.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- Currencies is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The major version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `3.0.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Added
- Added attribution for alyphen to the developers table in `README.md`

### Changed
- Documented that `/currency create` uses the item held in the main hand and accepts `--rename`/`--no-rename` to answer its rename prompt up front
- Corrected `/currency list`, which lists every active currency on the server rather than only the caller's faction's, and documented its `all`/`retired`/faction filters and page argument
- Documented that the amount argument of `/currency mint` is optional and defaults to `1`
- Documented the `confirm` argument of `/currency retire` and the confirmation prompt shown without it
- Documented that `/currency set name` and `/currency set description` fall back to a chat prompt when the new value is omitted
- Corrected the description of `currencies.showAmountMinted`, which controls the `Minted:` line in `/currency info` rather than a message shown after minting
- Corrected `currencies.powerCost`, which is a decimal charged per coin minted rather than an integer charged per mint operation
- Corrected the power cost wording throughout, since the cost is deducted from the minting player's power rather than faction power
- Corrected the description of `currencies.itemCostEnabled`, which consumes the currency's own item type per coin rather than a cost configured per currency

### Removed
- Removed Currencies 1 migration code (`legacy` package and the associated startup migration checks), since servers now run exclusively on Currencies 2

### Fixed
- Fixed `/currency create` with `--rename`/`--no-rename` failing with a database constraint error when a currency with that name already existed, because the duplicate-name check compared against the raw arguments (including the flag) instead of the parsed currency name

## [2.1.0]

### Changed
- Updated dependencies

## [2.0.0]

### Added
- Database-backed storage using jOOQ and Flyway
- HikariCP connection pooling
- Support for MariaDB and H2 databases
- PlaceholderAPI integration
- bStats metrics (plugin ID 12810)

### Changed
- Migrated codebase to Kotlin
- Rewrote currency and balance services

## [1.0.0]

### Added
- Initial release
- Faction-based currency creation and minting
- Coinpurse inventory system
- Power cost and item cost for minting
- Protection against crafting, smelting, placement, and anvil usage of currency items
