# World API

The World API provides functions for interacting with Minecraft worlds.

## Available Functions

### getWorld(name)

Retrieves a world by name.

```lua
local world = getWorld("world")
if world then
    info("World found: " .. world:getName())
else
    info("World not found")
end
```

**Parameters:**
- `name` (string): The world's name

**Returns:**
- World object or `nil` if world doesn't exist

### createExplosion(world, x, y, z, power)

Creates an explosion at the specified coordinates.

```lua
local world = getWorld("world")
if world then
    createExplosion(world, 100, 64, 100, 4.0)
    info("Explosion created!")
end
```

**Parameters:**
- `world` (World): The world to create explosion in
- `x` (number): X coordinate
- `y` (number): Y coordinate
- `z` (number): Z coordinate
- `power` (number): Explosion power (default TNT is 4.0)

## World Object Methods

When you have a World object, you can use these methods:

### Information Methods

```lua
local world = getWorld("world")

-- Get world name
local name = world:getName()

-- Get world type
local worldType = world:getWorldType():getName()

-- Get world difficulty
local difficulty = world:getDifficulty():name()

-- Check if world is loaded
local loaded = world:isLoaded()

-- Get spawn location
local spawn = world:getSpawnLocation()
local spawnX = spawn:getX()
local spawnY = spawn:getY()
local spawnZ = spawn:getZ()

-- Get world time
local time = world:getTime()
local fullTime = world:getFullTime()

-- Get weather information
local isStorming = world:hasStorm()
local isThundering = world:isThundering()

-- Get world border
local border = world:getWorldBorder()
local size = border:getSize()
local center = border:getCenter()
```

### World Manipulation Methods

```lua
local world = getWorld("world")

-- Set world time
world:setTime(0)  -- Morning
world:setTime(6000)  -- Noon
world:setTime(12000)  -- Evening
world:setTime(18000)  -- Night

-- Set spawn location
world:setSpawnLocation(0, 64, 0)

-- Control weather
world:setStorm(true)  -- Start rain
world:setStorm(false)  -- Stop rain
world:setThundering(true)  -- Start thunder
world:setThundering(false)  -- Stop thunder

-- Create explosion (alternative method)
world:createExplosion(100, 64, 100, 4.0, true, false)
```

### Block Methods

```lua
local world = getWorld("world")

-- Get block at location
local block = world:getBlockAt(100, 64, 100)
local blockType = block:getType()
local blockData = block:getBlockData()

-- Set block type
world:getBlockAt(100, 64, 100):setType(Material.DIRT)

-- Get highest block Y coordinate
local highestY = world:getHighestBlockYAt(100, 100)
```

### Entity Methods

```lua
local world = getWorld("world")

-- Get all entities in world
local entities = world:getEntities()

-- Get living entities only
local livingEntities = world:getLivingEntities()

-- Get players in world
local players = world:getPlayers()

-- Get nearby entities
local location = world:getSpawnLocation()
local nearbyEntities = world:getNearbyEntities(location, 10, 10, 10)
```

## Usage Examples

### World Information Command

```lua
registerCommand("worldinfo", nil, function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    sender:sendMessage("§6=== World Info ===")
    sender:sendMessage("§fWorld: " .. world:getName())
    sender:sendMessage("§fType: " .. world:getWorldType():getName())
    sender:sendMessage("§fDifficulty: " .. world:getDifficulty():name())
    sender:sendMessage("§fTime: " .. math.floor(world:getTime()))
    sender:sendMessage("§fWeather: " .. (world:hasStorm() and "Rainy" or "Clear"))
    sender:sendMessage("§fPosition: " .. math.floor(location:getX()) .. ", " .. math.floor(location:getY()) .. ", " .. math.floor(location:getZ()))
end)
```

### Set Spawn Command

```lua
registerCommand("setspawn", "plugin.setspawn", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    world:setSpawnLocation(location:getX(), location:getY(), location:getZ())
    player:sendMessage("§aSpawn set to your current location!")
end)
```

### Explosion Command

```lua
registerCommand("explode", "plugin.explode", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    -- Create explosion at player's location
    local power = tonumber(args[1]) or 4.0
    createExplosion(world, location:getX(), location:getY(), location:getZ(), power)
    
    player:sendMessage("§aCreated explosion with power " .. power)
end)
```

### Weather Control

```lua
registerCommand("weather", "plugin.weather", function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /weather <clear|rain|thunder>")
        return
    end
    
    local world
    local Player = Class("org.bukkit.entity.Player")
    if Player:isInstance(sender) then
        world = sender:getWorld()
    else
        world = getWorld("world")
    end
    
    local weatherType = args[1]:lower()
    
    if weatherType == "clear" then
        world:setStorm(false)
        world:setThundering(false)
        sender:sendMessage("§aWeather set to clear")
    elseif weatherType == "rain" then
        world:setStorm(true)
        world:setThundering(false)
        sender:sendMessage("§aWeather set to rain")
    elseif weatherType == "thunder" then
        world:setStorm(true)
        world:setThundering(true)
        sender:sendMessage("§aWeather set to thunder")
    else
        sender:sendMessage("§cInvalid weather type. Use: clear, rain, or thunder")
    end
end)
```

### Time Control

```lua
registerCommand("time", "plugin.time", function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /time <day|night|noon|midnight>")
        return
    end
    
    local world
    local Player = Class("org.bukkit.entity.Player")
    if Player:isInstance(sender) then
        world = sender:getWorld()
    else
        world = getWorld("world")
    end
    
    local timeType = args[1]:lower()
    
    if timeType == "day" then
        world:setTime(1000)
        sender:sendMessage("§aTime set to day")
    elseif timeType == "noon" then
        world:setTime(6000)
        sender:sendMessage("§aTime set to noon")
    elseif timeType == "night" then
        world:setTime(13000)
        sender:sendMessage("§aTime set to night")
    elseif timeType == "midnight" then
        world:setTime(18000)
        sender:sendMessage("§aTime set to midnight")
    else
        sender:sendMessage("§cInvalid time type. Use: day, night, noon, or midnight")
    end
end)
```

## Tips and Best Practices

1. Always check if a world exists before using its methods
2. Explosion power of 4.0 is equivalent to TNT
3. Weather and time changes affect all players in the world
4. Use world-specific data storage with metadata if needed
5. Consider the impact of explosions on nearby builds and players
6. Some world operations may cause lag in large areas