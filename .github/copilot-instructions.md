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
- `src/main/resources/` – `plugin.yml`, `config.yml`, and `lang/` files
- `src/test/kotlin/` – Unit tests
- `.github/workflows/` – CI and release workflows

## Coding Conventions

- Use the `lang/` resource files for every user-facing string; never hard-code
  messages in Kotlin.
- Follow the existing package structure when adding new classes.
- Annotate every command executor and event listener with `@Override` where applicable.
- This plugin depends on Medieval Factions; use its API where faction data is needed.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
