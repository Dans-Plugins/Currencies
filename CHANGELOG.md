# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
- Added attribution for alyphen to the developers table in `README.md`

### Changed
- Corrected documentation drift found by verifying `COMMANDS.md`, `CONFIG.md`, and `USER_GUIDE.md` against the command and listener sources: `/currency create` now documents its held-item requirement and `--rename`/`--no-rename` flags, `/currency list` documents its `all`/`retired`/faction filters and pagination, `/currency mint` documents that the amount is optional, `/currency retire` documents its `confirm` argument, `/currency set name` and `/currency set description` document their chat prompts, `currencies.showAmountMinted` is described as controlling the `Minted:` line in `/currency info`, `currencies.powerCost` is documented as a decimal charged per coin, and the power cost is described as the minting player's power rather than faction power

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
