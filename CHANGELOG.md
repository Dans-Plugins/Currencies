# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

- The usage-reporting "Details" link (startup notice, `config.yml` and the docs) now points at https://danielstephenson.dev/usage-reporting, a public page; the previous link led to a private repository and returned 404 for everyone. The vendored trace client is now 0.6.1, which carries the same link in the `plugins/trace/config.yml` header it writes. Details: https://github.com/Stephenson-Software/trace-client-java/releases/tag/0.6.1.

### Fixed

- `/currency info` no longer fails partway through on Paper (and other servers whose CraftBukkit package is not versioned): it threw `StringIndexOutOfBoundsException` before printing the item and the minted total. Where the item's details cannot be read, the plain item name is shown.
- `/currency mint` rejects a zero or negative amount ("The amount must be at least 1."). Previously `/currency mint Gold -5` credited the minting player power and lowered the currency's minted total (#225).
- `/currency set name` (and `/currency rename`) can change only the capitalization of a currency's name, e.g. `/currency set name Gold GOLD`. Previously the currency was found as a clash with itself and the rename was refused with "There is already a currency with that name." (#235).

### Changed

- Usage reporting is now disclosed on every startup: the plugin logs whether reporting is on (and what is sent, and how to turn it off) or off (and why). Two new ways to turn reporting off: `enabled: false` in `plugins/trace/config.yml` (created on first start, shared by every plugin that reports this way) and the environment variables `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`. The vendored trace client is 0.2.0. Nothing about what is sent changed; see the README's Usage reporting section
- The vendored trace client is now 0.3.0. `plugins/trace/config.yml` can now carry a `tags:` block whose entries are added to every usage event reported by the plugins on that server, with an event's own tag winning on a key clash; release test servers write `ci: "true"` there so their boots are left out of real-installation figures. A server without a `tags:` block reports exactly as before. Details: https://github.com/Stephenson-Software/trace-client-java/releases/tag/0.3.0.

### Fixed

- The plugin now closes its database connection pool when it disables, and when enabling fails part-way. Currencies shares `medieval_factions_db` with Medieval Factions and never closed its pool, so on every shutdown a connection of its own was left open; that kept the shared database from being closed cleanly and left H2's exit hook to fail against an unloaded classloader, writing `medieval_factions_db.trace.db` and, over time, corrupting the store (#219). An embedded H2 URL that is not in `AUTO_SERVER` mode is also opened with `DB_CLOSE_ON_EXIT=FALSE` (appended to `database.url` unless the setting is already present) so no such hook is registered at all; H2 refuses that setting together with `AUTO_SERVER=true`, so the default URL is left as configured. The close happens in `onDisable`, while the plugin is still loaded. A failed enable now logs the cause, closes the pool and disables the plugin instead of leaving the pool behind.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added
- The plugin now reports usage events — `startup` on enable, `command` on each use of `/currency` or `/coinpurse` — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The default config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is never rewritten by `saveDefaultConfig()`: the plugin reads the bundled defaults for any key the file lacks
- Added the project's first unit tests, covering `/currency` subcommand routing and its usage message. The Medieval Factions jar is now also on the test runtime classpath, which mocking the plugin class requires; it remains `compileOnly` for the shaded jar
- Extended the unit tests to `/currency set` routing and tab completion, and to `/currency balance`, whose report is built on the scheduler's async thread and is now exercised by capturing and running the scheduled task
- Extended the unit tests to `/currency retire`, covering its permission and faction-role gates, the confirmation prompt and its clickable confirm component, multi-word currency name resolution, and the save-failure path. Retiring a currency is one of the two supply-mutating commands, so its behaviour is now locked in against regression
- Extended the unit tests to `/currency list`, covering its permission gate, the `all`, `retired` and faction filters (by faction ID and by multi-word faction name, with and without a trailing page number), the invalid-filter, empty-result and out-of-range page messages, and tab completion
- Extended the unit tests to `/currency mint`, covering its permission and faction-role gates, the retired-currency refusal, default and explicit amounts, currency ID and multi-word (quoted and unquoted) name resolution, overflow coins dropped at the player's feet, the power and item costs, and both save-failure paths. Minting is the other supply-mutating command, so the minted total and the power deducted are now asserted. One test characterizes the current acceptance of a negative amount, reported as #225
- Extended the unit tests to `/currency set name`, covering its permission gate, the faction, role and change-name permission checks and the `currencies.force.rename` bypass, the name-clash refusal, currency ID and quoted multi-word name resolution, the save-failure path, the follow-up `/currency info`, the chat prompt used when the new name is omitted (including `cancel`), and tab completion. Two tests characterize current behaviour: an unquoted multi-word currency name is not resolved, and changing only the capitalization of a currency's own name is refused as a clash with itself, reported as #235
- Extended the unit tests to `/currency set description`, covering its permission gate, the faction, role and change-description permission checks and the `currencies.force.desc` bypass, currency ID and quoted multi-word name resolution, the save-failure path, the follow-up `/currency info`, the multi-line chat prompt used when the description is omitted (lines joined with spaces until `end`, and `cancel`), and tab completion. Two tests characterize current behaviour: an unquoted multi-word currency name is not resolved, and typing `end` before any line clears the description
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get currencies --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed
- Fixed the `/currency` usage message omitting the `rename` subcommand, which is both routed and tab-completed but was never advertised
- Fixed `/currency retire` and `/currency info` failing to resolve a currency whose name was given in double quotes. Bukkit splits arguments on spaces before the plugin sees them, and neither command stripped the quote characters afterwards, so `/currency retire "Gold Coin" confirm` reported that no such currency existed. Both commands now unquote their arguments, as `/currency mint`, `/currency set name` and `/currency set description` already did
- Fixed `/currency mint`, `/currency retire`, `/currency info`, `/currency set name` and `/currency set description` crashing with "An internal error occurred" when an argument was an empty or unbalanced pair of double quotes (`""` or `"`). The bundled Ponder `unquote` throws on that input; the five commands now fall back to the arguments as typed, so a malformed currency name is reported as unknown instead of crashing. Nothing was ever written before the crash, so no stored data is affected (#213)

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
