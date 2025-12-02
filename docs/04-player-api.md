# Player API

The Player API provides functions for interacting with players on the server.

## Available Functions

### getPlayer(name)

Retrieves a player by name.

```lua
local player = getPlayer("Steve")
if player then
    player:sendMessage("Found you!")
else
    print("Player not found")
end
```

**Parameters:**
- `name` (string): The player's name

**Returns:**
- Player object or `nil` if player is not online

### teleport(player, x, y, z)

Teleports a player to specific coordinates.

```lua
local player = getPlayer("Steve")
if player then
    teleport(player, 100, 64, 200)
    player:sendMessage("Teleported!")
end
```

**Parameters:**
- `player` (Player): The player to teleport
- `x` (number): X coordinate
- `y` (number): Y coordinate  
- `z` (number): Z coordinate

### kick(player, reason)

Kicks a player from the server with a reason.

```lua
local player = getPlayer("Steve")
if player then
    kick(player, "No griefing allowed!")
end
```

**Parameters:**
- `player` (Player): The player to kick
- `reason` (string, optional): Kick reason (default: "Kicked from server")

## Player Object Methods

When you have a Player object (from `getPlayer()` or event handlers), you can use these methods:

### Information Methods

```lua
local player = getPlayer("Steve")

-- Get player name
local name = player:getName()

-- Get player display name
local displayName = player:getDisplayName()

-- Get player's unique ID
local uuid = player:getUniqueId():toString()

-- Check if player is online
local online = player:isOnline()

-- Check if player is op
local isOp = player:isOp()

-- Get player's location
local location = player:getLocation()
local x = location:getX()
local y = location:getY()
local z = location:getZ()
local world = location:getWorld():getName()

-- Get player's health
local health = player:getHealth()
local maxHealth = player:getMaxHealth()

-- Get player's food level
local food = player:getFoodLevel()

-- Get player's experience
local exp = player:getExp()
local level = player:getLevel()
```

### Action Methods

```lua
local player = getPlayer("Steve")

-- Send message to player
player:sendMessage("Hello there!")

-- Send message with color codes
player:sendMessage("§aGreen text! §6Yellow text!")

-- Set player health
player:setHealth(20)

-- Set player food level
player:setFoodLevel(20)

-- Give player experience
player:giveExp(100)

-- Set player level
player:setLevel(5)

-- Clear player's inventory
player:getInventory():clear()

-- Make player fly
player:setAllowFlight(true)
player:setFlying(true)

-- Set player game mode
player:setGameMode(GameMode.CREATIVE)
player:setGameMode(GameMode.SURVIVAL)
player:setGameMode(GameMode.ADVENTURE)
player:setGameMode(GameMode.SPECTATOR)
```

### Permission Methods

```lua
local player = getPlayer("Steve")

-- Check if player has permission
if player:hasPermission("plugin.admin") then
    player:sendMessage("You have admin permissions!")
end

-- Give player operator status
player:setOp(true)

-- Remove operator status
player:setOp(false)
```

### Inventory Methods

```lua
local player = getPlayer("Steve")

-- Get player's inventory
local inventory = player:getInventory()

-- Get item in main hand
local item = player:getInventory():getItemInMainHand()

-- Get item in off hand
local offHand = player:getInventory():getItemInOffHand()

-- Get helmet
local helmet = player:getInventory():getHelmet()

-- Get chestplate
local chestplate = player:getInventory():getChestplate()

-- Get leggings
local leggings = player:getInventory():getLeggings()

-- Get boots
local boots = player:getInventory():getBoots()
```

## Usage Examples

### Welcome Message

```lua
on("PlayerJoinEvent", function(event)
    local player = event:getPlayer()
    player:sendMessage("§aWelcome to the server, " .. player:getName() .. "!")
    
    -- Give new players a starter kit
    if not player:hasPlayedBefore() then
        local inventory = player:getInventory()
        addItem(inventory, "WOODEN_SWORD", 1)
        addItem(inventory, "BREAD", 10)
        player:sendMessage("§6You received a starter kit!")
    end
end)
```

### Player Stats Command

```lua
registerCommand("stats", nil, 10, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local location = player:getLocation()
    
    sender:sendMessage("§6=== Your Stats ===")
    sender:sendMessage("§fHealth: " .. math.floor(player:getHealth()) .. "/" .. player:getMaxHealth())
    sender:sendMessage("§fFood: " .. player:getFoodLevel() .. "/20")
    sender:sendMessage("§fLevel: " .. player:getLevel())
    sender:sendMessage("§fLocation: " .. math.floor(location:getX()) .. ", " .. math.floor(location:getY()) .. ", " .. math.floor(location:getZ()))
    sender:sendMessage("§fWorld: " .. location:getWorld():getName())
end)
```

### Teleport to Player

```lua
registerCommand("tp", "plugin.tp", 5, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    if not args[1] then
        sender:sendMessage("Usage: /tp <player>")
        return
    end
    
    local target = getPlayer(args[1])
    if not target then
        sender:sendMessage("Player not found: " .. args[1])
        return
    end
    
    local targetLocation = target:getLocation()
    teleport(sender, targetLocation:getX(), targetLocation:getY(), targetLocation:getZ())
    sender:sendMessage("Teleported to " .. target:getName())
end)
```

### Heal Command

```lua
registerCommand("heal", "plugin.heal", 30, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    player:setHealth(player:getMaxHealth())
    player:setFoodLevel(20)
    
    -- Remove potion effects
    for _, effect in ipairs(player:getActivePotionEffects()) do
        player:removePotionEffect(effect:getType())
    end
    
    player:sendMessage("§aYou have been fully healed!")
end)
```

## Tips and Best Practices

1. Always check if a player exists before using their methods
2. Use `isInstanceOf()` to check if the sender is a player
3. Color codes use `§` symbol (e.g., `§a` for green, `§c` for red)
4. Check player permissions before performing administrative actions