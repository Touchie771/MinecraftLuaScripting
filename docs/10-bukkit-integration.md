# Bukkit Integration

The plugin provides direct access to Bukkit classes and enums, allowing you to use the full power of the Bukkit API from your Lua scripts.

## Available Bukkit Classes

The following Bukkit classes are exposed as global variables in Lua:

- `Bukkit` - Main Bukkit class for server operations
- `Material` - All material types (blocks, items, etc.)
- `EntityType` - All entity types (mobs, animals, etc.)
- `GameMode` - Game mode enum values

## Using Bukkit Classes

### Bukkit Class

The Bukkit class provides static methods for server operations:

```lua
-- Get server instance
local server = Bukkit:getServer()

-- Broadcast messages
Bukkit:broadcastMessage("§aHello everyone!")

-- Get worlds
local worlds = Bukkit:getWorlds()
for _, world in ipairs(worlds) do
    info("World: " .. world:getName())
end

-- Get online players
local players = Bukkit:getOnlinePlayers()
for _, player in ipairs(players) do
    info("Online: " .. player:getName())
end

-- Get world by name
local world = Bukkit:getWorld("world_nether")

-- Create a world
Bukkit:createWorld(WorldCreator:name("custom_world"))

-- Get plugin manager
local pluginManager = Bukkit:getPluginManager()

-- Dispatch commands
Bukkit:dispatchCommand(Bukkit:getConsoleSender(), "say Hello from Lua!")

-- Get scheduler
local scheduler = Bukkit:getScheduler()
```

### Material Enum

Use the Material enum to work with blocks and items:

```lua
-- Check material types
if block:getType() == Material.DIAMOND_ORE then
    player:sendMessage("You found diamond!")
end

-- Create item stacks
local diamondSword = ItemStack(Material.DIAMOND_SWORD, 1)
local woodPlank = ItemStack(Material.OAK_PLANKS, 64)

-- Set block types
block:setType(Material.GOLD_BLOCK)

-- Common materials
Material.STONE
Material.DIRT
Material.GRASS_BLOCK
Material.OAK_LOG
Material.DIAMOND
Material.GOLD_INGOT
Material.IRON_INGOT
Material.COAL
Material.TORCH
Material.CHEST
Material.FURNACE
```

### EntityType Enum

Use EntityType to work with different entity types:

```lua
-- Check entity types
if entity:getType() == EntityType.ZOMBIE then
    player:sendMessage("Watch out for the zombie!")
end

-- Spawn entities
world:spawnEntity(location, EntityType.COW)

-- Common entity types
EntityType.COW
EntityType.PIG
EntityType.SHEEP
EntityType.CHICKEN
EntityType.ZOMBIE
EntityType.SKELETON
EntityType.SPIDER
EntityType.CREEPER
EntityType.ENDERMAN
EntityType.VILLAGER
EntityType.IRON_GOLEM
EntityType.WOLF
EntityType.HORSE
EntityType.ENDER_DRAGON
EntityType.WITHER
```

### GameMode Enum

Use GameMode to set player game modes:

```lua
-- Set game modes
player:setGameMode(GameMode.CREATIVE)
player:setGameMode(GameMode.SURVIVAL)
player:setGameMode(GameMode.ADVENTURE)
player:setGameMode(GameMode.SPECTATOR)

-- Check game mode
if player:getGameMode() == GameMode.CREATIVE then
    player:sendMessage("You are in creative mode!")
end
```

## Advanced Bukkit Usage

### World Creator

```lua
-- Create custom worlds
local worldCreator = Bukkit:createWorld()
worldCreator:name("skyblock")
worldCreator:type(WorldType.FLAT)
worldCreator:generator("void")
worldCreator:generateStructures(false)
local newWorld = Bukkit:createWorld(worldCreator)
```

### Plugin Manager

```lua
-- Get plugin information
local plugins = Bukkit:getPluginManager():getPlugins()
for _, plugin in ipairs(plugins) do
    info("Plugin: " .. plugin:getName() .. " v" .. plugin:getDescription():getVersion())
end

-- Check if plugin is enabled
local worldEdit = Bukkit:getPluginManager():getPlugin("WorldEdit")
if worldEdit and worldEdit:isEnabled() then
    info("WorldEdit is available!")
end
```

### Scheduler

```lua
-- Schedule tasks
local scheduler = Bukkit:getScheduler()

-- Run task later (in ticks)
scheduler:runTaskLater(plugin, function()
    Bukkit:broadcastMessage("Delayed message!")
end, 20 * 5)  -- 5 seconds later

-- Run repeating task
scheduler:runTaskTimer(plugin, function()
    for _, player in ipairs(Bukkit:getOnlinePlayers()) do
        if player:getHealth() < player:getMaxHealth() then
            player:heal(1)
        end
    end
end, 0, 20)  -- Every second

-- Run async task
scheduler:runTaskAsynchronously(plugin, function()
    -- Perform async operation
    -- Note: Cannot access Bukkit API directly in async tasks
end)
```

### Configuration

```lua
-- Access server properties
local server = Bukkit:getServer()

-- Get server properties
local motd = server:getMotd()
local maxPlayers = server:getMaxPlayers()
local serverPort = server:getPort()
local serverIp = server:getIp()

-- Modify server properties
server:setMotd("§6Custom Server MOTD")
server:setWhitelist(true)
```

## Complete Examples

### Advanced Block Protection

```lua
local protectedBlocks = {}

-- Protect blocks with a command
registerCommand("protect", "plugin.protect", 0, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("Players only!")
        return
    end
    
    local player = sender
    local block = player:getTargetBlock(nil, 5)
    
    if block:getType() == Material.AIR then
        player:sendMessage("§cLook at a block to protect it!")
        return
    end
    
    local location = block:getLocation()
    local key = math.floor(location:getX()) .. "," .. 
               math.floor(location:getY()) .. "," .. 
               math.floor(location:getZ())
    
    protectedBlocks[key] = player:getName()
    player:sendMessage("§aBlock protected!")
end)

-- Prevent breaking protected blocks
on("BlockBreakEvent", function(event)
    local block = event:getBlock()
    local location = block:getLocation()
    local key = math.floor(location:getX()) .. "," .. 
               math.floor(location:getY()) .. "," .. 
               math.floor(location:getZ())
    
    if protectedBlocks[key] then
        event:setCancelled(true)
        event:getPlayer():sendMessage("§cThis block is protected by " .. protectedBlocks[key])
    end
end)
```

### Custom Mob Spawner

```lua
-- Spawn custom mobs with effects
registerCommand("spawnboss", "plugin.spawnboss", 300, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("Players only!")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    -- Spawn wither
    local wither = world:spawnEntity(location, EntityType.WITHER)
    
    -- Set custom name
    wither:setCustomName("§c§lBOSS WITHER")
    wither:setCustomNameVisible(true)
    
    -- Set max health
    wither:setMaxHealth(300)
    wither:setHealth(300)
    
    -- Create explosion effect
    world:createExplosion(location:getX(), location:getY(), location:getZ(), 2.0, false, false)
    
    Bukkit:broadcastMessage("§c§lA boss wither has been spawned by " .. player:getName() .. "!")
end)
```

### World Border System

```lua
registerCommand("setborder", "plugin.border", 0, function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /setborder <size>")
        return
    end
    
    local size = tonumber(args[1])
    if not size or size < 1 then
        sender:sendMessage("§cInvalid size!")
        return
    end
    
    local world = sender:getWorld()
    local border = world:getWorldBorder()
    
    border:setSize(size)
    border:setCenter(0, 0)
    border:setWarningDistance(10)
    border:setWarningTime(5)
    
    sender:sendMessage("§aWorld border set to " .. size .. "x" .. size)
end)

-- Prevent leaving border
on("PlayerMoveEvent", function(event)
    local player = event:getPlayer()
    local location = player:getLocation()
    local world = player:getWorld()
    local border = world:getWorldBorder()
    
    if not border:isInside(location) then
        event:setCancelled(true)
        player:sendMessage("§cYou cannot leave the world border!")
    end
end)
```

## Tips and Best Practices

1. Use the colon syntax (`:`) for calling Bukkit methods (e.g., `Bukkit:broadcastMessage()`)
2. Check if objects exist before using their methods
3. Use Material and EntityType enums for type safety
4. Combine Bukkit API with the plugin's helper functions for easier coding
5. Be careful with operations that affect all players
6. Use proper permissions for administrative Bukkit operations
7. Consider performance when using Bukkit methods in frequent events
8. Remember that some Bukkit operations require async handling for large tasks