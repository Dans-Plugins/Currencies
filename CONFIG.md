# Configuration Guide

All configuration options are found in `plugins/Currencies/config.yml`. The file is generated automatically on first run.

---

## version

**Type:** string
**Default:** *(set automatically)*
**Description:** The plugin version. This value is managed automatically and should not be changed manually.

---

## database.url

**Type:** string
**Default:** `jdbc:h2:./medieval_factions_db;AUTO_SERVER=true;MODE=MYSQL;DATABASE_TO_UPPER=false`
**Description:** The JDBC connection URL for the database. By default an embedded H2 database is used. Change this to a MariaDB/MySQL URL to use an external database.

**Example (MariaDB):**
```yaml
database:
  url: 'jdbc:mariadb://localhost:3306/currencies'
  dialect: 'MARIADB'
  username: 'currencies_user'
  password: 'secret'
```

---

## database.dialect

**Type:** string
**Default:** `H2`
**Description:** The SQL dialect to use. Supported values: `H2`, `MARIADB`.

---

## database.username

**Type:** string
**Default:** `sa`
**Description:** The database username.

---

## database.password

**Type:** string
**Default:** *(empty)*
**Description:** The database password.

---

## coinpurse.slots

**Type:** integer
**Default:** `54`
**Description:** The number of inventory slots in a player's coinpurse. Must be a multiple of 9 and no greater than 54.

**Example:**
```yaml
coinpurse:
  slots: 27
```

---

## currencies.showAmountMinted

**Type:** boolean
**Default:** `true`
**Description:** When `true`, `/currency info` includes a `Minted:` line showing the total amount of that currency minted so far. When `false`, the line is omitted.

**Example:**
```yaml
currencies:
  showAmountMinted: false
```

---

## currencies.powerCostEnabled

**Type:** boolean
**Default:** `true`
**Description:** When `true`, minting a currency costs the minting player power.

**Example:**
```yaml
currencies:
  powerCostEnabled: false
```

---

## currencies.powerCost

**Type:** decimal
**Default:** `1`
**Description:** The amount of power deducted from the minting player per coin minted, so minting `n` coins costs `n × powerCost`. Decimal values are supported. Only relevant when `powerCostEnabled` is `true`.

**Example:**
```yaml
currencies:
  powerCost: 0.5
```

---

## currencies.itemCostEnabled

**Type:** boolean
**Default:** `true`
**Description:** When `true`, minting consumes one item of the currency's own item type per coin minted. The item type is whatever was held when the currency was created, so the cost is fixed per currency rather than configured here.

**Example:**
```yaml
currencies:
  itemCostEnabled: false
```

---

## currencies.disallowCrafting

**Type:** boolean
**Default:** `true`
**Description:** When `true`, currency items cannot be used in crafting recipes.

**Example:**
```yaml
currencies:
  disallowCrafting: false
```

---

## currencies.disallowSmelting

**Type:** boolean
**Default:** `true`
**Description:** When `true`, currency items cannot be smelted in a furnace.

**Example:**
```yaml
currencies:
  disallowSmelting: false
```

---

## currencies.disallowPlacement

**Type:** boolean
**Default:** `true`
**Description:** When `true`, currency items cannot be placed as blocks in the world.

**Example:**
```yaml
currencies:
  disallowPlacement: false
```

---

## currencies.disallowAnvilUsage

**Type:** boolean
**Default:** `true`
**Description:** When `true`, currency items cannot be combined or renamed in an anvil.

**Example:**
```yaml
currencies:
  disallowAnvilUsage: false
```
