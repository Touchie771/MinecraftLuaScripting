# Entity API

The Entity API provides functions for spawning and interacting with entities in Minecraft.

## Available Functions

### spawnEntity(world, x, y, z, type)

Spawns an entity at the specified coordinates.

```lua
local world = getWorld("world")
if world then
    local entity = spawnEntity(world, 100, 64, 100, "ZOMBIE")
    if entity then
        info("Spawned a zombie!")
    end
end
```

**Parameters:**
- `world` (World): The world to spawn entity in
- `x` (number): X coordinate
- `y` (number): Y coordinate
- `z` (number): Z coordinate
- `type` (string): Entity type name (case-insensitive)

**Returns:**
- Entity object or error message if type is invalid

## Entity Object Methods

When you have an Entity object, you can use these methods:

### Information Methods

```lua
-- Get entity type
local entityType = entity:getType():name()

-- Get entity's unique ID
local uuid = entity:getUniqueId():toString()

-- Get entity location
local location = entity:getLocation()
local x = location:getX()
local y = location:getY()
local z = location:getZ()

-- Check if entity is alive
local alive = entity:isAlive()

-- Check if entity is on ground
local onGround = entity:isOnGround()

-- Get entity's custom name
local customName = entity:getCustomName()

-- Check if entity has AI
local hasAI = entity:hasAI()

-- Get entity's fire ticks
local fireTicks = entity:getFireTicks()
```

### Entity Manipulation Methods

```lua
-- Set entity on fire
entity:setFire(100)  -- 100 ticks

-- Extinguish entity
entity:setFire(0)

-- Set entity's custom name
entity:setCustomName("My Pet")

-- Make custom name visible
entity:setCustomNameVisible(true)

-- Remove entity
entity:remove()

-- Teleport entity
entity:teleport(location)

-- Damage entity
entity:damage(5)  -- 5 hearts damage

-- Heal entity
entity:heal(10)  -- 10 hearts healed
```

### Living Entity Methods

If the entity is a living entity (player, mob, etc.):

```lua
-- Get health
local health = entity:getHealth()
local maxHealth = entity:getMaxHealth()

-- Set health
entity:setHealth(20)

-- Get last damage cause
local damageCause = entity:getLastDamageCause():getCause():name()

-- Check if entity is leashed
local isLeashed = entity:isLeashed()

-- Get entity's equipment
local equipment = entity:getEquipment()
local mainHand = equipment:getItemInMainHand()
```

## Usage Examples

### Spawn Mob Command

```lua
registerCommand("spawnmob", "plugin.spawnmob", 10, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    if not args[1] then
        sender:sendMessage("Usage: /spawnmob <type> [amount]")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    local mobType = args[1]:upper()
    local amount = tonumber(args[2]) or 1
    
    if amount > 10 then
        sender:sendMessage("§cCannot spawn more than 10 mobs at once!")
        return
    end
    
    local spawned = 0
    for i = 1, amount do
        local entity = spawnEntity(world, location:getX(), location:getY(), location:getZ(), mobType)
        if entity then
            spawned = spawned + 1
        end
    end
    
    if spawned > 0 then
        sender:sendMessage("§aSpawned " .. spawned .. " " .. mobType .. "(s)!")
    else
        sender:sendMessage("§cInvalid entity type: " .. mobType)
    end
end)
```

### Butcher Command

```lua
registerCommand("butcher", "plugin.butcher", 30, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    local radius = tonumber(args[1]) or 10
    
    local killed = 0
    for _, entity in ipairs(world:getLivingEntities()) do
        if entity:getType() ~= EntityType.PLAYER then
            local entityLoc = entity:getLocation()
            local distance = entityLoc:distance(location)
            
            if distance <= radius then
                entity:remove()
                killed = killed + 1
            end
        end
    end
    
    player:sendMessage("§aKilled " .. killed .. " entities within " .. radius .. " blocks!")
end)
```

### Entity Info Command

```lua
registerCommand("entityinfo", "plugin.entityinfo", 0, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local world = player:getWorld()
    local location = player:getLocation()
    
    -- Find nearest entity
    local nearestEntity = nil
    local nearestDistance = 10
    
    for _, entity in ipairs(world:getEntities()) do
        if entity ~= player then
            local entityLoc = entity:getLocation()
            local distance = entityLoc:distance(location)
            
            if distance < nearestDistance then
                nearestDistance = distance
                nearestEntity = entity
            end
        end
    end
    
    if nearestEntity then
        sender:sendMessage("§6=== Nearest Entity ===")
        sender:sendMessage("§fType: " .. nearestEntity:getType():name())
        sender:sendMessage("§fDistance: " .. string.format("%.2f", nearestDistance))
        sender:sendMessage("§fHealth: " .. math.floor(nearestEntity:getHealth() or 0))
        sender:sendMessage("§fCustom Name: " .. (nearestEntity:getCustomName() or "None"))
        sender:sendMessage("§fUUID: " .. nearestEntity:getUniqueId():toString():sub(1, 8))
    else
        sender:sendMessage("§cNo entities found nearby.")
    end
end)
```

### Pet System

```lua
local pets = {}

on("PlayerInteractEntityEvent", function(event)
    local player = event:getPlayer()
    local entity = event:getRightClicked()
    
    if player:isSneaking() and entity:isInstanceOf(org.bukkit.entity.LivingEntity) then
        if entity:getType() ~= EntityType.PLAYER then
            -- Make entity a pet
            entity:setCustomName(player:getName() .. "'s Pet")
            entity:setCustomNameVisible(true)
            entity:setAI(false)  -- Freeze the entity
            pets[player:getUniqueId():toString()] = entity
            
            player:sendMessage("§aYou now have a pet!")
        end
    end
end)

registerCommand("pet", nil, 0, function(sender, args)
    if not sender:isInstanceOf(org.bukkit.entity.Player) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local playerId = player:getUniqueId():toString()
    local pet = pets[playerId]
    
    if not pet then
        player:sendMessage("§cYou don't have a pet. Sneak and right-click an entity to claim one!")
        return
    end
    
    if not pet:isAlive() then
        pets[playerId] = nil
        player:sendMessage("§cYour pet died!")
        return
    end
    
    -- Teleport pet to player
    pet:teleport(player:getLocation())
    player:sendMessage("§aYour pet has been teleported to you!")
end)
```

### Mob Arena Spawner

```lua
on("PlayerCommandPreprocessEvent", function(event)
    local command = event:getMessage()
    local player = event:getPlayer()
    
    if command == "/spawnwave" then
        event:setCancelled(true)
        
        if not player:hasPermission("arena.spawn") then
            player:sendMessage("§cNo permission!")
            return
        end
        
        local world = player:getWorld()
        local arenaLocation = player:getLocation()
        
        -- Spawn a wave of zombies
        for i = 1, 5 do
            local x = arenaLocation:getX() + math.random(-10, 10)
            local z = arenaLocation:getZ() + math.random(-10, 10)
            local zombie = spawnEntity(world, x, arenaLocation:getY(), z, "ZOMBIE")
            
            if zombie then
                zombie:setCustomName("Wave " .. i)
                zombie:setCustomNameVisible(true)
            end
        end
        
        Bukkit:broadcastMessage("§cA new wave has spawned! Defend the arena!")
    end
end)
```

## Entity Types

Common entity types you can spawn:
- Animals: `COW`, `PIG`, `SHEEP`, `CHICKEN`, `HORSE`, `WOLF`, `OCELOT`
- Mobs: `ZOMBIE`, `SKELETON`, `SPIDER`, `CREEPER`, `ENDERMAN`
- Nether: `PIGLIN`, `HOGLIN`, `ZOMBIFIED_PIGLIN`, `GHAST`, `BLAZE`
- End: `ENDERMAN`, `ENDER_DRAGON`, `SHULKER`
- Water: `SQUID`, `DOLPHIN`, `GUARDIAN`, `ELDER_GUARDIAN`
- Others: `VILLAGER`, `IRON_GOLEM`, `SNOWMAN`, `WITCH`

## Tips and Best Practices

1. Always check if the entity type is valid before spawning
2. Limit the number of entities spawned to prevent lag
3. Use proper permissions for entity spawning commands
4. Consider removing entities after they're no longer needed
5. Check if entities are still alive before operating on them
6. Be careful with explosive entities like Creepers and Ghasts