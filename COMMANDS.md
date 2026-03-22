# Commands Reference

## Currency Commands

### /currency balance

**Description:** View your current balance of all currencies.
**Permission:** `currencies.balance`
**Usage:** `/currency balance`
**Example:** `/currency balance`

---

### /currency create \<name\>

**Description:** Create a new currency for your faction.
**Permission:** `currencies.create`
**Usage:** `/currency create <name>`
**Example:** `/currency create GoldCoin`

---

### /currency info \<currency\>

**Description:** View information about a specific currency.
**Permission:** `currencies.list`
**Usage:** `/currency info <currency>`
**Example:** `/currency info GoldCoin`

---

### /currency list

**Description:** List all currencies associated with your faction.
**Permission:** `currencies.list`
**Usage:** `/currency list`
**Example:** `/currency list`

---

### /currency mint \<currency\> \<amount\>

**Description:** Mint coins of the specified currency. May cost faction power and/or items depending on server configuration.
**Permission:** `currencies.mint`
**Usage:** `/currency mint <currency> <amount>`
**Example:** `/currency mint GoldCoin 10`

---

### /currency rename \<currency\> \<new-name\>

**Description:** Rename an existing currency. Requires the appropriate faction permission or `currencies.force.rename`.
**Permission:** `currencies.rename`
**Usage:** `/currency rename <currency> <new-name>`
**Example:** `/currency rename GoldCoin SilverCoin`

---

### /currency set description \<currency\> \<description\>

**Description:** Set or update the description of a currency. Requires the appropriate faction permission or `currencies.force.desc`.
**Permission:** `currencies.desc`
**Usage:** `/currency set description <currency> <description>`
**Example:** `/currency set description GoldCoin "The official coin of the Northern Kingdom"`

---

### /currency retire \<currency\>

**Description:** Permanently retire a currency so it can no longer be minted.
**Permission:** `currencies.retire`
**Usage:** `/currency retire <currency>`
**Example:** `/currency retire GoldCoin`

---

## Coinpurse Commands

### /coinpurse

**Description:** Open your coinpurse inventory to view and organise your coins.
**Aliases:** `/purse`, `/wallet`
**Permission:** `currencies.coinpurse`
**Usage:** `/coinpurse`
**Example:** `/coinpurse`
