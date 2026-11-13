# Commands Reference

## Currency Commands

### /currency balance

**Description:** View how many coins of each currency are stored in your coinpurse. Every currency on the server is listed, with retired ones marked `[retired]`. Coins carried in your inventory are not counted.
**Permission:** `currencies.balance`
**Usage:** `/currency balance`
**Example:** `/currency balance`

---

### /currency create \<name\> \[--rename|--no-rename\]

**Description:** Create a new currency for your faction. The item held in your main hand is used as the currency's item, so an item must be held when the command is run.
**Permission:** `currencies.create`
**Usage:** `/currency create <name> [--rename|--no-rename]`
**Example:** `/currency create GoldCoin --rename`

If the held item's display name does not already match the currency name, the command stops and offers a clickable choice rather than creating the currency: **Yes** re-runs the command with `--rename` (the item is renamed to the currency name), and **No** re-runs it with `--no-rename` (the item keeps its own name). Passing either flag directly skips the prompt.

---

### /currency info \<currency\>

**Description:** View information about a specific currency.
**Permission:** None (available to all players)
**Usage:** `/currency info <currency>`
**Example:** `/currency info GoldCoin`

A currency name containing spaces may be given either as-is or wrapped in double quotes: `/currency info Gold Coin` and `/currency info "Gold Coin"` both resolve.

---

### /currency list \[all|retired|\<faction\>\] \[page\]

**Description:** List currencies. With no filter, every active currency on the server is listed. `all` lists active and retired currencies, `retired` lists only retired ones, and a faction name or faction ID lists only that faction's currencies. Results are paginated; a trailing page number selects a page (pages are numbered from 1).
**Permission:** `currencies.list`
**Usage:** `/currency list [all|retired|<faction>] [page]`
**Example:** `/currency list retired 2`

---

### /currency mint \<currency\> \[amount\]

**Description:** Mint coins of the specified currency. May cost the minting player power and/or items depending on server configuration. The amount is optional and defaults to `1` when omitted; it must be at least `1`. Minted coins are placed in your inventory, and any that do not fit are dropped at your feet. If saving the mint fails, no coins are given and any power or items taken as its cost are handed back.
**Permission:** `currencies.mint`
**Usage:** `/currency mint <currency> [amount]`
**Example:** `/currency mint GoldCoin 10`

A currency name containing spaces may be given either as-is or wrapped in double quotes: `/currency mint Gold Coin 10` and `/currency mint "Gold Coin" 10` both resolve. A trailing whole number is read as the amount, not as part of the name.

---

### /currency set name \<currency\> \[new-name\]

**Description:** Set the name of an existing currency. Requires the appropriate faction permission or `currencies.force.rename`. When the new name is omitted, it is asked for in a chat prompt, which can be exited by typing `cancel`. The coin item keeps its existing display name, both on coins already in circulation and on coins minted afterwards ([#182](https://github.com/Dans-Plugins/Currencies/issues/182)).
**Aliases:** `/currency rename <currency> [new-name]`
**Permission:** `currencies.rename`
**Usage:** `/currency set name <currency> [new-name]`
**Example:** `/currency set name GoldCoin SilverCoin`

Only the first argument names the currency, so a currency name containing spaces must be wrapped in double quotes: `/currency set name "Gold Coin" Silver Coin` renames `Gold Coin` to `Silver Coin`, while `/currency set name Gold Coin Silver Coin` looks up a currency named `Gold` instead (and, if one exists, renames it to `Coin Silver Coin`). Everything after the currency name is taken as the new name, quoted or not.

---

### /currency rename \<currency\> \[new-name\]

**Description:** Alias for `/currency set name`. Rename an existing currency. Requires the appropriate faction permission or `currencies.force.rename`. When the new name is omitted, it is asked for in a chat prompt, which can be exited by typing `cancel`. The coin item keeps its existing display name, both on coins already in circulation and on coins minted afterwards ([#182](https://github.com/Dans-Plugins/Currencies/issues/182)).
**Permission:** `currencies.rename`
**Usage:** `/currency rename <currency> [new-name]`
**Example:** `/currency rename GoldCoin SilverCoin`

As with `/currency set name`, a currency name containing spaces must be wrapped in double quotes: `/currency rename "Gold Coin" Silver Coin`.

---

### /currency set description \<currency\> \[description\]

**Description:** Set or update the description of a currency. Requires the appropriate faction permission or `currencies.force.desc`. When the description is omitted, it is asked for in a chat prompt, which accepts several lines of text, joined with spaces, until `end` is typed; typing `cancel` exits without saving.
**Aliases:** `/currency set desc <currency> [description]`
**Permission:** `currencies.desc`
**Usage:** `/currency set description <currency> [description]`
**Example:** `/currency set description GoldCoin "The official coin of the Northern Kingdom"`

Only the first argument names the currency, so a currency name containing spaces must be wrapped in double quotes: `/currency set description "Gold Coin" The official coin of the Northern Kingdom`. Without the quotes, the first word alone is looked up as the currency.

---

### /currency retire \<currency\> \[confirm\]

**Description:** Permanently retire a currency so it can no longer be minted. Without the trailing `confirm` argument, a clickable confirmation prompt is shown instead of retiring the currency; `confirm` performs the retirement and notifies the owning faction.
**Permission:** `currencies.retire`
**Usage:** `/currency retire <currency> [confirm]`
**Example:** `/currency retire GoldCoin confirm`

A currency name containing spaces may be given either as-is or wrapped in double quotes: `/currency retire Gold Coin confirm` and `/currency retire "Gold Coin" confirm` both resolve.

---

## Coinpurse Commands

### /coinpurse

**Description:** Open your coinpurse inventory to store coins and take them out. When it is closed, the coins left in it are saved as your balance, and items that are not coins of a currency are dropped at your feet. If your balance does not fit when it opens, the coins that do not fit are dropped at your feet.
**Aliases:** `/purse`, `/wallet`
**Permission:** `currencies.coinpurse`
**Usage:** `/coinpurse`
**Example:** `/coinpurse`
