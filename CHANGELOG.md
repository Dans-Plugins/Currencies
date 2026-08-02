# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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
