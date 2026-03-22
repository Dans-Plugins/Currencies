# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Kotlin
- Build tool: Gradle (Groovy DSL)
- Target platform: Spigot / Paper
- Test framework: JUnit 5 + MockK

## Project Structure

- `src/main/kotlin/` – Plugin source code
- `src/main/resources/` – `plugin.yml`, `config.yml`, and other resource files
- `src/test/kotlin/` – Unit tests
- `.github/workflows/` – CI and release workflows

## Coding Conventions

- Prefer using `lang/` resource files for user-facing strings. When introducing new
  messages, avoid hard-coding them in Kotlin where a suitable `lang/` resource
  structure already exists in this project.
- Follow the existing package structure when adding new classes.
- In Kotlin, use the `override` keyword for overridden members, and annotate Bukkit event listener methods with `@EventHandler`.
- This plugin depends on Medieval Factions; use its API where faction data is needed.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
