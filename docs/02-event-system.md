# Event System

The event system allows you to listen to and respond to various Minecraft server events.

## Registering Event Listeners

Use the `on()` function to register an event listener:

```lua
on("EventName", function(event)
    -- Handle the event
end)
```

## Supported Event Categories

Events are automatically detected from these Bukkit event packages:
- `org.bukkit.event.*`
- `org.bukkit.event.player.*`
- `org.bukkit.event.entity.*`
- `org.bukkit.event.block.*`
- `org.bukkit.event.world.*`
- `org.bukkit.event.inventory.*`
- `org.bukkit.event.server.*`
- `org.bukkit.event.vehicle.*`
- `org.bukkit.event.weather.*`
- `org.bukkit.event.hanging.*`
- `org.bukkit.event.enchantment.*`
- `org.bukkit.event.raid.*`

## Common Events

### Player Events

```lua
-- When a player joins the server
on("PlayerJoinEvent", function(event)
    local player = event:getPlayer()
    player:sendMessage("Welcome back!")
end)

-- When a player quits the server
on("PlayerQuitEvent", function(event)
    local player = event:getPlayer()
    info(player:getName() .. " has left the server")
end)

-- When a player chats
on("AsyncPlayerChatEvent", function(event)
    local message = event:getMessage()
    local player = event:getPlayer()
    
    -- Block bad words
    if string.find(message, "badword") then
        event:setCancelled(true)
        player:sendMessage("Please watch your language!")
    end
end)

-- When a player moves
on("PlayerMoveEvent", function(event)
    local player = event:getPlayer()
    local location = player:getLocation()
    
    -- Prevent players from entering a restricted area
    if location:getX() > 1000 then
        player:teleport(location:subtract(10, 0, 0))
        player:sendMessage("You cannot enter this area!")
    end
end)
```

### Block Events

```lua
-- When a block is broken
on("BlockBreakEvent", function(event)
    local player = event:getPlayer()
    local block = event:getBlock()
    
    -- Log broken blocks
    info(player:getName() .. " broke " .. block:getType():toString())
    
    -- Prevent breaking diamond ore without permission
    if block:getType() == Material.DIAMOND_ORE then
        if not player:hasPermission("break.diamond") then
            event:setCancelled(true)
            player:sendMessage("You cannot break diamond ore!")
        end
    end
end)

-- When a block is placed
on("BlockPlaceEvent", function(event)
    local player = event:getPlayer()
    local block = event:getBlock()
    
    -- Prevent placing TNT
    if block:getType() == Material.TNT then
        event:setCancelled(true)
        player:sendMessage("TNT is not allowed!")
    end
end)
```

### Entity Events

```lua
-- When an entity spawns
on("EntitySpawnEvent", function(event)
    local entity = event:getEntity()
    
    -- Limit mob spawning in certain areas
    local location = entity:getLocation()
    if location:getX() < 100 and location:getX() > -100 then
        if entity:getType() == EntityType.ZOMBIE then
            event:setCancelled(true)
        end
    end
end)

-- When an entity dies
on("EntityDeathEvent", function(event)
    local entity = event:getEntity()
    local killer = entity:getKiller()
    
    -- Give rewards for killing bosses
    if entity:getType() == EntityType.ENDER_DRAGON and killer then
        killer:sendMessage("You defeated the Ender Dragon!")
        addItem(getInventory(killer), Material.DIAMOND, 64)
    end
end)
```

## Event Object Methods

The event object passed to your handler has various methods depending on the event type. Here are some common ones:

### Cancellable Events
Many events can be cancelled:
```lua
on("PlayerInteractEvent", function(event)
    if someCondition then
        event:setCancelled(true)  -- Prevent the action
    end
end)
```

### Getting Event Information
```lua
on("BlockBreakEvent", function(event)
    local player = event:getPlayer()      -- Get the player
    local block = event:getBlock()        -- Get the block
    local world = block:getWorld()        -- Get the world
    local location = block:getLocation()  -- Get the location
end)
```

## Error Handling

Event errors are caught and logged:
```lua
on("PlayerJoinEvent", function(event)
    -- This error won't crash the plugin
    local player = event:getNonExistentMethod()
end)
```

## Performance Tips

1. Avoid expensive operations in frequently called events (like PlayerMoveEvent)
2. Use conditions to filter events early
3. Cache expensive calculations
4. Consider using cooldowns for actions in high-frequency events

## Multiple Listeners

You can register multiple listeners for the same event:
```lua
-- First listener
on("PlayerJoinEvent", function(event)
    event:getPlayer():sendMessage("Welcome!")
end)

-- Second listener
on("PlayerJoinEvent", function(event)
    info("Player joined: " .. event:getPlayer():getName())
end)
```

Both listeners will be called when a player joins.