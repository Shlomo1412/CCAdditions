# CC Additions — API Reference

CC Additions is a CC: Tweaked addon mod for Minecraft 1.20.1 (Forge). It adds 7 peripherals and 2 utility items.

---

## Table of Contents

- [Scanner](#scanner)
- [Advanced Scanner](#advanced-scanner)
- [Fingerprint Reader](#fingerprint-reader)
- [Player Connector](#player-connector)
- [Computerized TNT](#computerized-tnt)
- [Ship Reader (VS2)](#ship-reader)
- [Ship Controller (VS2)](#ship-controller)
- [Remote Terminal (Item)](#remote-terminal)
- [Advanced Remote Terminal (Item)](#advanced-remote-terminal)

---

## Scanner

**Peripheral type:** `scanner`

Scans blocks and entities in a configurable range around the scanner. Max range is **256 blocks** per axis.

### Functions

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `scan(xRange, yRange, zRange)` | `int, int, int` | `table[]` | Scans blocks in range. Air is skipped. |
| `scanForEntities(xRange, yRange, zRange)` | `int, int, int` | `table[]` | Scans entities in range. |
| `getMaxRange()` | — | `number` | Returns `256`. |
| `getPosition()` | — | `{x, y, z}` | Scanner's world position. |

### Block Scan Result

```lua
{
  x = 3, y = -1, z = 0,       -- relative to scanner
  name = "minecraft:stone",
  state = { ... },             -- block state properties
  metadata = {
    hardness = 1.5,
    requiresToolForDrops = true,
    lightEmission = 0,
    isFlammable = false,
    hasBlockEntity = false
  }
}
```

### Entity Scan Result

```lua
{
  x = 5.2, y = 0.0, z = -3.1, -- relative to scanner
  name = "minecraft:zombie",
  displayName = "Zombie",
  uuid = "...",
  metadata = {
    isAlive = true, isOnGround = true,
    isInWater = false, isOnFire = false,
    motionX = 0.0, motionY = -0.08, motionZ = 0.0,
    yaw = 90.0, pitch = 0.0,
    health = 20.0, maxHealth = 20.0,
    armorValue = 2,
    isBaby = false, isPlayer = false
  }
}
```

### Example

```lua
local scanner = peripheral.find("scanner")
local blocks = scanner.scan(8, 4, 8)
for _, b in ipairs(blocks) do
  print(b.name .. " at " .. b.x .. "," .. b.y .. "," .. b.z)
end
```

---

## Advanced Scanner

**Peripheral type:** `advanced_scanner`

Like the Scanner but with **no range limit** and the ability to scan from a remote origin point. Only scans loaded chunks.

### Functions

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `scan(xRange, yRange, zRange)` | `int, int, int` | `table[]` | Scans blocks from the current origin. No range limit. |
| `scanForEntities(xRange, yRange, zRange)` | `int, int, int` | `table[]` | Scans entities from the current origin. |
| `setScanPosition(x, y, z)` | `int, int, int` | — | Sets a custom scan origin (remote scanning). |
| `getScanPosition()` | — | `{x, y, z, isCustom}` | Returns current scan origin. |
| `resetScanPosition()` | — | — | Resets origin to the scanner's own position. |
| `getPosition()` | — | `{x, y, z}` | Scanner's actual world position. |
| `getBlockAt(x, y, z)` | `int, int, int` | `table?` | Gets a single block at an absolute position. |
| `getBlocksInCubic(x1,y1,z1, x2,y2,z2)` | `int ×6` | `table[]` | Gets all blocks in an absolute bounding box. |
| `getEntitiesInCubic(x1,y1,z1, x2,y2,z2)` | `int ×6` | `table[]` | Gets all entities in an absolute bounding box. |
| `scanForShips()` | — | `table[]` | Gets all VS2 ships in the world. Returns `{}` if VS2 is not installed. |

Results include both **relative** (`x`, `y`, `z`) and **absolute** (`absoluteX`, `absoluteY`, `absoluteZ`) coordinates.

### Ship Scan Result (requires VS2)

```lua
{
  id = 12345,
  name = "my_ship",
  position = { x = 100.5, y = 64.0, z = -200.3 },
  rotation = { w = 1.0, x = 0.0, y = 0.0, z = 0.0 },
  rotationEuler = { pitch = 0.0, yaw = 45.0, roll = 0.0 },
  velocity = { x = 0.0, y = 0.0, z = 5.0 },
  angularVelocity = { x = 0.0, y = 0.1, z = 0.0 },
  mass = 15000.0,
  isStatic = false
}
```

### Example

```lua
local scanner = peripheral.find("advanced_scanner")

-- Remote scan: check a position 1000 blocks away
scanner.setScanPosition(1000, 64, 1000)
local blocks = scanner.scan(16, 8, 16)

-- Get one specific block
local block = scanner.getBlockAt(100, 64, 200)
if block then print(block.name) end
```

---

## Fingerprint Reader

**Peripheral type:** `fingerprint_reader`

A wall-mounted block. When a player right-clicks it, a `"fingerprint"` event is fired to all connected computers with comprehensive player data.

### Functions

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `getHelp()` | — | `string` | Returns usage instructions. |

### Events

When a player right-clicks the block, a `"fingerprint"` event is queued with a large data table:

| Section | Fields |
|---------|--------|
| `identity` | `name`, `uuid`, `displayName`, `team`, `teamColor` |
| `health` | `current`, `max`, `absorption`, `armor`, `armorToughness`, `isDead` |
| `food` | `hunger`, `saturation`, `exhaustion`, `needsFood` |
| `position` | `x`, `y`, `z`, `blockX`, `blockY`, `blockZ`, `yaw`, `pitch`, `dimension`, `biome`, `lightLevel` |
| `movement` | `velocityX/Y/Z`, `speed`, `horizontalSpeed`, `fallDistance`, `walkDistance` |
| `state` | `isOnGround`, `isSneaking`, `isSprinting`, `isSwimming`, `isFlying`, `isGliding`, `isSleeping`, `isBlocking`, `isInWater`, `isInLava`, `isOnFire`, `isInvisible`, ... |
| `experience` | `level`, `progress`, `total`, `pointsToNextLevel` |
| `gameInfo` | `gameMode`, `isCreative`, `isSpectator`, `canFly`, `score`, `ping` |
| `attributes` | `maxHealth`, `movementSpeed`, `attackDamage`, `attackSpeed`, `luck`, ... |
| `inventory` | Full inventory as item tables |
| `hotbar` | Hotbar slots (9 items) |
| `armor` | Armor slots (4 items) |
| `heldItems` | Main and off-hand items |
| `effects` | Active potion effects with `name`, `duration`, `amplifier` |
| `enderChest` | Ender chest contents |
| `statistics` | Player statistics |

### Example

```lua
local reader = peripheral.find("fingerprint_reader")

while true do
  local event, data = os.pullEvent("fingerprint")
  print("Player: " .. data.identity.name)
  print("Health: " .. data.health.current .. "/" .. data.health.max)
  print("Position: " .. data.position.x .. ", " .. data.position.y .. ", " .. data.position.z)
end
```

---

## Player Connector

**Peripheral type:** `player_connector`

Pairs to a specific player and provides real-time data about them. Right-click the block to pair. All functions return `nil` if the player is offline or unpaired.

### Functions

**Identity:**

| Function | Returns | Description |
|----------|---------|-------------|
| `isPaired()` | `boolean` | Whether a player is paired. |
| `getPairedPlayerName()` | `string?` | Paired player's name. |
| `getPairedPlayerUUID()` | `string?` | Paired player's UUID. |
| `isOnline()` | `boolean` | Whether paired player is online. |

**Health & Stats:**

| Function | Returns |
|----------|---------|
| `getHealth()` | Current health |
| `getMaxHealth()` | Maximum health |
| `getAbsorption()` | Absorption hearts |
| `getHunger()` | Food level (0–20) |
| `getSaturation()` | Saturation level |
| `getAir()` / `getMaxAir()` | Air supply |
| `getArmorValue()` | Armor points |

**Position & Movement:**

| Function | Returns |
|----------|---------|
| `getPosition()` | `{x, y, z}` (doubles) |
| `getBlockPosition()` | `{x, y, z}` (integers) |
| `getDimension()` | Dimension ID string |
| `getVelocity()` | `{x, y, z}` |
| `getSpeed()` | Total 3D speed |
| `getHorizontalSpeed()` | XZ speed |
| `getRotation()` | `{yaw, pitch}` |

**State Checks** (all return `boolean?`):

`isSneaking()` · `isCrouching()` · `isSprinting()` · `isRunning()` · `isWalking()` · `isSwimming()` · `isFlying()` · `isOnGround()` · `isInWater()` · `isInLava()` · `isOnFire()` · `isSleeping()` · `isBlocking()` · `isGliding()` · `isInvisible()`

**Experience & Game:**

| Function | Returns |
|----------|---------|
| `getExperienceLevel()` | XP level |
| `getExperienceProgress()` | XP bar (0.0–1.0) |
| `getTotalExperience()` | Total XP |
| `getGameMode()` | `"survival"`, `"creative"`, etc. |
| `isCreative()` / `isSpectator()` | `boolean?` |
| `getScore()` | Player score |

**Inventory:**

| Function | Returns |
|----------|---------|
| `getInventory()` | Full inventory (36 slots) |
| `getHotbar()` | Hotbar (9 slots) |
| `getSelectedSlot()` | Selected slot index |
| `getMainHandItem()` / `getOffHandItem()` | Item table |
| `getArmorItems()` | 4 armor slots |

**Effects & Attributes:**

| Function | Returns |
|----------|---------|
| `getEffects()` | Active potion effects |
| `hasEffect(name)` | `boolean?` — partial name match |
| `getMovementSpeed()` | Movement speed attribute |
| `getAttackDamage()` / `getAttackSpeed()` | Combat attributes |
| `getLuck()` | Luck attribute |

**Misc:**

| Function | Returns |
|----------|---------|
| `getBedPosition()` | `{x, y, z}?` |
| `getRespawnPosition()` | `{x, y, z, dimension}?` |

### Example

```lua
local connector = peripheral.find("player_connector")

if connector.isPaired() and connector.isOnline() then
  local pos = connector.getPosition()
  print(connector.getPairedPlayerName() .. " is at " .. pos.x .. ", " .. pos.y .. ", " .. pos.z)
  print("Health: " .. connector.getHealth() .. "/" .. connector.getMaxHealth())
end
```

---

## Computerized TNT

**Peripheral type:** `computerized_tnt`

Programmable TNT. Set the fuse time and explosion strength, then ignite or defuse remotely.

**Limits:**
- Fuse: **1–6000 ticks** (0.05s – 5 minutes). Default: 80 ticks (4s).
- Strength: **1.0–20.0**. Default: 4.0 (normal TNT). Max is 5× vanilla.

### Functions

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `ignite()` | — | `boolean` | Start the countdown. Returns `false` if already ignited. |
| `defuse()` | — | `boolean` | Stop the countdown. Returns `false` if not ignited. |
| `isIgnited()` | — | `boolean` | Whether counting down. |
| `getRemainingFuse()` | — | `number` | Ticks left, or `-1` if not ignited. |
| `setFuse(ticks)` | `int` | `boolean` | Set fuse time. Must be set before ignition. |
| `getFuse()` | — | `number` | Current fuse setting. |
| `setStrength(power)` | `number` | `boolean` | Set explosion power. Must be set before ignition. |
| `getStrength()` | — | `number` | Current explosion power. |

### Example

```lua
local tnt = peripheral.find("computerized_tnt")

tnt.setFuse(100)       -- 5 seconds
tnt.setStrength(8.0)   -- 2x normal TNT
tnt.ignite()

-- Monitor countdown
while tnt.isIgnited() do
  print("Fuse: " .. tnt.getRemainingFuse() .. " ticks")
  sleep(0.5)
end
```

---

## Ship Reader

**Peripheral type:** `ship_reader`

*Requires Valkyrien Skies 2.* Wall-mountable block that reads all data from the VS2 ship it's placed on. Only appears in the creative tab when VS2 is installed.

### Functions

| Function | Returns | Description |
|----------|---------|-------------|
| `isOnShip()` | `boolean` | Whether this block is on a ship. |
| `getShipId()` | `number` | Ship ID, or `-1`. |
| `getShipName()` | `string?` | Ship slug/name. |
| `getPosition()` | `{x, y, z}?` | Ship world position (doubles). |
| `getRotation()` | `{w, x, y, z}?` | Quaternion rotation. |
| `getRotationEuler()` | `{pitch, yaw, roll}?` | Euler angles in degrees. |
| `getVelocity()` | `{x, y, z}?` | Linear velocity (m/s). |
| `getAngularVelocity()` | `{x, y, z}?` | Angular velocity (rad/s). |
| `getMass()` | `number` | Mass in kg, or `0`. |
| `isStatic()` | `boolean` | Whether physics are disabled. |
| `getAll()` | `table` | All data in one call (see below). |

### `getAll()` Result

```lua
{
  isOnShip = true,
  id = 12345,
  name = "my_ship",
  position = { x = 100.5, y = 64.0, z = -200.3 },
  rotation = { w = 1.0, x = 0.0, y = 0.0, z = 0.0 },
  rotationEuler = { pitch = 0.0, yaw = 45.0, roll = 0.0 },
  velocity = { x = 0.0, y = 0.0, z = 5.0 },
  angularVelocity = { x = 0.0, y = 0.1, z = 0.0 },
  mass = 15000.0,
  isStatic = false
}
```

### Example

```lua
local reader = peripheral.find("ship_reader")

if reader.isOnShip() then
  local data = reader.getAll()
  print("Ship: " .. data.name)
  print("Position: " .. data.position.x .. ", " .. data.position.y .. ", " .. data.position.z)
  print("Speed: " .. math.sqrt(data.velocity.x^2 + data.velocity.y^2 + data.velocity.z^2) .. " m/s")
end
```

---

## Ship Controller

**Peripheral type:** `ship_controller`

*Requires Valkyrien Skies 2.* Wall-mountable block that can both read ship data and apply forces/velocities. Only appears in the creative tab when VS2 is installed.

**Limits:**
- Max force/torque: **1,000,000** N / Nm
- Max velocity: **1,000** m/s

### Read Functions

Same as Ship Reader: `isOnShip()`, `getPosition()`, `getRotation()`, `getVelocity()`, `getAngularVelocity()`, `getMass()`, `isStatic()`.

### Force & Torque

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `applyWorldForce(x, y, z)` | `double ×3` | `boolean` | Force in world coordinates (N). |
| `applyWorldTorque(x, y, z)` | `double ×3` | `boolean` | Torque in world coordinates (Nm). |
| `applyModelForce(x, y, z)` | `double ×3` | `boolean` | Force in ship-relative coordinates. Rotates with the ship. |
| `applyModelTorque(x, y, z)` | `double ×3` | `boolean` | Torque in ship-relative coordinates. |
| `applyForceAtPosition(fx, fy, fz, px, py, pz)` | `double ×6` | `boolean` | Force at a specific ship position (creates both linear force and torque). |

### Velocity

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `setVelocity(x, y, z)` | `double ×3` | `boolean` | Set linear velocity directly. |
| `setAngularVelocity(x, y, z)` | `double ×3` | `boolean` | Set angular velocity directly. |
| `addVelocity(x, y, z)` | `double ×3` | `boolean` | Add to current linear velocity. |
| `addAngularVelocity(x, y, z)` | `double ×3` | `boolean` | Add to current angular velocity. |

### State

| Function | Parameters | Returns | Description |
|----------|-----------|---------|-------------|
| `setStatic(isStatic)` | `boolean` | `boolean` | Enable/disable physics. Static ships ignore forces and gravity. |

### Example

```lua
local ctrl = peripheral.find("ship_controller")

if ctrl.isOnShip() then
  -- Hover: counteract gravity based on mass
  local mass = ctrl.getMass()
  ctrl.applyWorldForce(0, mass * 10, 0)  -- 10 m/s² ≈ gravity

  -- Thrust forward in ship space
  ctrl.applyModelForce(0, 0, 50000)

  -- Spin
  ctrl.applyWorldTorque(0, 10000, 0)
end
```

---

## Remote Terminal

*Not a peripheral — this is an item.*

A handheld device that pairs to a CC: Tweaked computer and opens a **view-only** remote terminal screen.

| Action | Effect |
|--------|--------|
| Right-click a computer | Pairs to that computer |
| Right-click in air | Opens the paired terminal (view-only) |
| Shift + right-click in air | Unpairs |
| Right-click a wall/floor/ceiling | Places as a wall-mounted display |

---

## Advanced Remote Terminal

*Not a peripheral — this is an item.*

Same as the Remote Terminal but with rare (blue) item name and an enchanted glint when paired. Future versions will support cross-dimension viewing and full input control.

---

## Summary

| Peripheral | Type String | Functions | Notes |
|-----------|-------------|:---------:|-------|
| Scanner | `scanner` | 4 | Max range 256 |
| Advanced Scanner | `advanced_scanner` | 10 | Unlimited range, remote origin, VS2 ship scan |
| Fingerprint Reader | `fingerprint_reader` | 1 + event | Wall-mounted, event-driven |
| Player Connector | `player_connector` | 42 | Pair to any player |
| Computerized TNT | `computerized_tnt` | 8 | Configurable fuse & strength |
| Ship Reader | `ship_reader` | 11 | VS2 required, read-only |
| Ship Controller | `ship_controller` | 17 | VS2 required, full control |
