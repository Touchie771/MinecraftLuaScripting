# Logger API

The Logger API provides functions for logging messages from your Lua scripts to the server console.

## Available Functions

### info(message)

Logs an informational message to the server console.

```lua
info("Script loaded successfully!")
info("Player " .. playerName .. " performed an action")
```

**Parameters:**
- `message` (string): The message to log

### warning(message)

Logs a warning message to the server console.

```lua
warning("Deprecated function used!")
warning("Player " .. playerName .. " tried to exploit something")
```

**Parameters:**
- `message` (string): The warning message to log

### error(message)

Logs an error message to the server console.

```lua
error("Failed to process player data!")
error("Critical error in event handler")
```

**Parameters:**
- `message` (string): The error message to log

## Usage Examples

### Basic Logging

```lua
-- Log when script starts
info("MyScript v1.0 has been loaded!")

-- Log player actions
on("PlayerJoinEvent", function(event)
    local player = event:getPlayer()
    info("Player joined: " .. player:getName() .. " (UUID: " .. player:getUniqueId():toString() .. ")")
end)

on("BlockBreakEvent", function(event)
    local player = event:getPlayer()
    local block = event:getBlock()
    info(player:getName() .. " broke " .. block:getType():toString() .. " at " .. 
         math.floor(block:getX()) .. "," .. math.floor(block:getY()) .. "," .. math.floor(block:getZ()))
end)
```

### Warning Logs

```lua
-- Warn about deprecated usage
registerCommand("oldcommand", nil, 0, function(sender, args)
    warning("Player " .. sender:getName() .. " used deprecated command 'oldcommand'")
    sender:sendMessage("This command is deprecated. Please use 'newcommand' instead.")
end)

-- Warn about potential issues
on("PlayerChatEvent", function(event)
    local message = event:getMessage()
    if string.len(message) > 200 then
        warning("Player " .. event:getPlayer():getName() .. " sent very long message (" .. string.len(message) .. " chars)")
    end
end)
```

### Error Logs

```lua
-- Log critical errors
registerCommand("riskycommand", "plugin.risky", 0, function(sender, args)
    local success, err = pcall(function()
        -- Risky operation
        local result = someRiskyFunction(args[1])
        return result
    end)
    
    if not success then
        error("Failed to execute risky command for " .. sender:getName() .. ": " .. tostring(err))
        sender:sendMessage("§cAn error occurred while executing your command.")
    end
end)

-- Log configuration errors
local config = {
    maxPlayers = 100,
    spawnRadius = 50
}

if config.maxPlayers > 200 then
    error("Configuration error: maxPlayers cannot exceed 200")
end

if config.spawnRadius < 10 then
    warning("Configuration warning: spawnRadius is very small (" .. config.spawnRadius .. ")")
end
```

### Debug Logging System

```lua
-- Create a debug system that can be toggled
local DEBUG_MODE = true

function debug(message)
    if DEBUG_MODE then
        info("[DEBUG] " .. message)
    end
end

-- Usage in events
on("EntityDamageEvent", function(event)
    debug("Entity damage: " .. event:getDamage() .. " to " .. event:getEntity():getType():name())
    
    if event:getDamage() > 20 then
        warning("High damage detected: " .. event:getDamage())
    end
end)

-- Toggle debug mode with command
registerCommand("debug", "plugin.debug", 0, function(sender, args)
    if not sender:hasPermission("plugin.debug.toggle") then
        sender:sendMessage("§cNo permission!")
        return
    end
    
    if args[1] == "on" then
        DEBUG_MODE = true
        info("Debug mode enabled by " .. sender:getName())
        sender:sendMessage("§aDebug mode enabled")
    elseif args[1] == "off" then
        DEBUG_MODE = false
        info("Debug mode disabled by " .. sender:getName())
        sender:sendMessage("§aDebug mode disabled")
    else
        sender:sendMessage("Usage: /debug <on|off>")
    end
end)
```

### Performance Logging

```lua
-- Log performance metrics
local startTime = os.time()

on("PlayerMoveEvent", function(event)
    -- This event fires frequently, so we log sparingly
    if math.random(1000) == 1 then  -- Log 0.1% of moves
        local player = event:getPlayer()
        debug("Random move sample: " .. player:getName() .. " at " .. 
              math.floor(player:getLocation():getX()) .. "," .. 
              math.floor(player:getLocation():getZ()))
    end
end)

-- Log script performance
local function measurePerformance(func, name)
    local start = os.clock()
    local result = func()
    local duration = os.clock() - start
    
    if duration > 0.1 then  -- Log if takes more than 100ms
        warning("Performance warning: " .. name .. " took " .. string.format("%.3f", duration) .. " seconds")
    end
    
    return result
end

-- Usage
registerCommand("heavycommand", "plugin.heavy", 0, function(sender, args)
    measurePerformance(function()
        -- Heavy operation here
        for i = 1, 10000 do
            -- Some calculation
        end
        sender:sendMessage("§aOperation completed!")
    end, "heavycommand")
end)
```

### Audit Logging

```lua
-- Create an audit log for important actions
function audit(action, player, details)
    local timestamp = os.date("%Y-%m-%d %H:%M:%S")
    info("[AUDIT] " .. timestamp .. " - " .. action .. " by " .. player:getName() .. 
         (details and (" - " .. details) or ""))
end

-- Log important commands
registerCommand("ban", "plugin.ban", 0, function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /ban <player> [reason]")
        return
    end
    
    local target = getPlayer(args[1])
    if target then
        local reason = args[2] or "Banned by operator"
        executeAs(Bukkit:getConsoleSender(), "ban " .. target:getName() .. " " .. reason)
        
        audit("BAN", sender, "target=" .. target:getName() .. " reason=" .. reason)
        sender:sendMessage("§aPlayer " .. target:getName() .. " has been banned!")
    end
end)

-- Log important events
on("PlayerAdvancementDoneEvent", function(event)
    local player = event:getPlayer()
    local advancement = event:getAdvancement():getDisplay():getDisplayName()
    
    audit("ADVANCEMENT", player, "advancement=" .. tostring(advancement))
    info(player:getName() .. " completed advancement: " .. tostring(advancement))
end)
```

## Log Levels

The logger API provides three levels of logging:

1. **info()** - General information messages
   - Shown in console with `[INFO]` prefix
   - Use for normal operation messages

2. **warning()** - Warning messages
   - Shown in console with `[WARNING]` prefix
   - Use for potential issues or deprecated usage

3. **error()** - Error messages
   - Shown in console with `[SEVERE]` prefix
   - Use for critical errors and failures

## Tips and Best Practices

1. Use descriptive log messages that include relevant context
2. Include player names and other identifying information when applicable
3. Use warning() for issues that don't break functionality but should be noted
4. Use error() for critical failures that need immediate attention
5. Avoid logging in high-frequency events (like PlayerMoveEvent) unless necessary
6. Consider implementing a debug mode toggle for development
7. Log important administrative actions for audit purposes
8. Use timestamps for better log analysis
9. Don't log sensitive information like passwords or private data
10. Use structured logging with consistent formats for easier parsing