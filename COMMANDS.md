# Commands Reference

## Currency Commands

### /currency balance

**Description:** View your current balance of all currencies.
**Permission:** `currencies.balance`
**Usage:** `/currency balance`
**Example:** `/currency balance`

---

### /currency create \<name\> \[--rename|--no-rename\]

**Description:** Create a new currency for your faction. The item held in your main hand is used as the currency's item, so an item must be held when the command is run.

If the held item's display name does not already match the currency name, the command stops and offers a clickable choice rather than creating the currency: **Yes** re-runs the command with `--rename` (the item is renamed to the currency name), and **No** re-runs it with `--no-rename` (the item keeps its own name). Passing either flag directly skips the prompt.
**Permission:** `currencies.create`
**Usage:** `/currency create <name> [--rename|--no-rename]`
**Example:** `/currency create GoldCoin --rename`

---

### /currency info \<currency\>

**Description:** View information about a specific currency.
**Permission:** None (available to all players)
**Usage:** `/currency info <currency>`
**Example:** `/currency info GoldCoin`

---

### /currency list \[all|retired|\<faction\>\] \[page\]

**Description:** List currencies. With no filter, every active currency on the server is listed. `all` lists active and retired currencies, `retired` lists only retired ones, and a faction name or faction ID lists only that faction's currencies. Results are paginated; a trailing page number selects a page (pages are numbered from 1).
**Permission:** `currencies.list`
**Usage:** `/currency list [all|retired|<faction>] [page]`
**Example:** `/currency list retired 2`

---

### /currency mint \<currency\> \[amount\]

**Description:** Mint coins of the specified currency. May cost the minting player power and/or items depending on server configuration. The amount is optional and defaults to `1` when omitted.
**Permission:** `currencies.mint`
**Usage:** `/currency mint <currency> [amount]`
**Example:** `/currency mint GoldCoin 10`

---

### /currency set name \<currency\> \[new-name\]

**Description:** Set the name of an existing currency. Requires the appropriate faction permission or `currencies.force.rename`. When the new name is omitted, it is asked for in a chat prompt, which can be exited by typing `cancel`.
**Aliases:** `/currency rename <currency> [new-name]`
**Permission:** `currencies.rename`
**Usage:** `/currency set name <currency> [new-name]`
**Example:** `/currency set name GoldCoin SilverCoin`

---

### /currency rename \<currency\> \[new-name\]

**Description:** Alias for `/currency set name`. Rename an existing currency. Requires the appropriate faction permission or `currencies.force.rename`. When the new name is omitted, it is asked for in a chat prompt, which can be exited by typing `cancel`.
**Permission:** `currencies.rename`
**Usage:** `/currency rename <currency> [new-name]`
**Example:** `/currency rename GoldCoin SilverCoin`

---

### /currency set description \<currency\> \[description\]

**Description:** Set or update the description of a currency. Requires the appropriate faction permission or `currencies.force.desc`. When the description is omitted, it is asked for in a chat prompt, which can be exited by typing `cancel`.
**Aliases:** `/currency set desc <currency> [description]`
**Permission:** `currencies.desc`
**Usage:** `/currency set description <currency> [description]`
**Example:** `/currency set description GoldCoin "The official coin of the Northern Kingdom"`

---

### /currency retire \<currency\> \[confirm\]

**Description:** Permanently retire a currency so it can no longer be minted. Without the trailing `confirm` argument, a clickable confirmation prompt is shown instead of retiring the currency; `confirm` performs the retirement and notifies the owning faction.
**Permission:** `currencies.retire`
**Usage:** `/currency retire <currency> [confirm]`
**Example:** `/currency retire GoldCoin confirm`

---

## Coinpurse Commands

### /coinpurse

**Description:** Open your coinpurse inventory to view and organise your coins.
**Aliases:** `/purse`, `/wallet`
**Permission:** `currencies.coinpurse`
**Usage:** `/coinpurse`
**Example:** `/coinpurse`
