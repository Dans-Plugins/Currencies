# User Guide

## Prerequisites

- A Spigot or Paper Minecraft server (1.13 or later)
- [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) installed and configured

## First Steps

After installing Currencies and restarting your server:

1. Ensure you are a member of a faction in Medieval Factions.
2. Hold the item you want to use as the currency's coin, then use `/currency create <name>` to create a new currency for your faction.
3. Use `/currency mint <currency> [amount]` to mint coins of your currency.
4. Open your coinpurse with `/coinpurse` to view your current coins.

## Common Scenarios

### Creating a Currency

A faction leader or a player with the appropriate faction permission can create a new currency. The item held in the main hand becomes the currency's coin, so an item must be held when the command is run:

```
/currency create GoldCoin
```

If the held item's display name does not already match the currency name, a **Yes**/**No** prompt is shown instead of creating the currency straight away. **Yes** renames the item to match the currency, and **No** keeps the item's existing name. The prompt can be skipped by passing the choice up front:

```
/currency create GoldCoin --rename
/currency create GoldCoin --no-rename
```

### Minting Currency

Once a currency exists, authorised faction members can mint coins. Minting costs the minting player power (configurable) and may require an item cost. The amount defaults to `1` when omitted:

```
/currency mint GoldCoin 10
```

### Viewing Your Balance

Check how many coins of each currency you hold:

```
/currency balance
```

### Viewing Currency Information

Inspect the details of a specific currency:

```
/currency info <currency>
```

### Listing Currencies

List every active currency on the server:

```
/currency list
```

The list can be filtered, and a trailing page number selects a page of results:

```
/currency list all
/currency list retired
/currency list MyFaction
/currency list all 2
```

### Renaming a Currency

Rename an existing currency (requires faction permission). Leaving the new name off asks for it in a chat prompt, which can be exited by typing `cancel`:

```
/currency rename <currency> <new-name>
```

### Setting a Currency Description

Set or update the description of a currency (requires faction permission). Leaving the description off asks for it in a chat prompt, which can be exited by typing `cancel`:

```
/currency set description <currency> <description>
```

### Retiring a Currency

Permanently retire a currency so it can no longer be minted (requires faction permission). A confirmation prompt is shown first; retirement only happens once it is confirmed:

```
/currency retire <currency>
```

### Using Your Coinpurse

Open your coinpurse inventory to view and organise your coins:

```
/coinpurse
```

## Permissions

| Permission | Default | Description |
|---|---|---|
| `currencies.coinpurse` | true | Allows opening your coinpurse |
| `currencies.balance` | true | Allows viewing your balance |
| `currencies.create` | true | Allows creating currencies |
| `currencies.list` | true | Allows listing currencies |
| `currencies.mint` | true | Allows minting currencies |
| `currencies.retire` | true | Allows retiring currencies |
| `currencies.desc` | true | Allows setting the description of currencies |
| `currencies.force.desc` | op | Allows setting the description of currencies without being a member of the faction or having the faction permission |
| `currencies.rename` | true | Allows setting the name of currencies |
| `currencies.force.rename` | op | Allows setting the name of currencies without being a member of the faction or having the faction permission |
