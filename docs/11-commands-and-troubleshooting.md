# Plugin Commands and Troubleshooting

## Plugin Commands

The plugin provides built-in administrative commands for managing Lua scripts.

### /luascript reloadall

Reloads all Lua scripts from the `LuaScripts` folder.

**Permission:** `luascript.admin` (default: OP)

**Usage:**
```
/luascript reloadall
```

**What it does:**
- Unregisters all existing event listeners
- Reloads all `.lua` files from the `LuaScripts` folder
- Logs the loading process to console

**Example:**
```
/luascript reloadall
§eReloading all scripts...
§aReloaded all scripts!
```

## Common Issues and Solutions

### StackOverflowError with VarArgFunction

**Error Message:**
```
java.lang.StackOverflowError: null
    at org.luaj.vm2.lib.VarArgFunction.onInvoke(Unknown Source)
    at org.luaj.vm2.lib.VarArgFunction.invoke(Unknown Source)
```

**Cause:** Incorrect method override when implementing VarArgFunction

**Solution:** Make sure to override `invoke(Varargs args)` instead of `call()`:

```lua
-- WRONG
public LuaValue call(LuaValue arg1, LuaValue arg2) {
    // This causes infinite recursion
}

-- CORRECT
public Varargs invoke(Varargs args) {
    LuaValue arg1 = args.arg(1);
    LuaValue arg2 = args.arg(2);
    // Process arguments here
    return LuaValue.TRUE;
}
```

### Command Registration Fails

**Symptoms:** Command doesn't work after registration

**Common Causes:**
1. Command name already exists
2. Invalid callback function
3. CommandMap not initialized

**Solutions:**
```lua
-- Check if callback is a function
if not callbackVal.isfunction() then
    error("Callback must be a function")
end

-- Use unique command names
registerCommand("my_unique_command", nil, callback)

-- Check for errors during registration
if commandMap == nil then
    error("CommandMap not initialized")
end
```

### Permission Errors

**Symptoms:** Command says "permission denied" even with no permission set

**Cause:** Null permission string passed to Bukkit

**Solution:** Check permission before setting it:
```lua
if permission and permission ~= "" then
    setPermission(permission)
end
```

### Event Listener Not Working

**Symptoms:** Event handler function never called

**Common Causes:**
1. Incorrect event name
2. Event class not found
3. Script not loaded

**Solutions:**
```lua
-- Use correct event class names
on("PlayerJoinEvent", handler)  -- Correct
on("PlayerJoin", handler)       -- Wrong

-- Check if event was registered successfully
local result = on("UnknownEvent", handler)
if not result.toboolean() then
    error("Event registration failed")
end
```

### Lua Syntax Errors

**Common Syntax Issues:**

1. **Colon vs Dot for Bukkit methods:**
```lua
-- Correct - Use colon for instance methods
Bukkit:broadcastMessage("Hello")
player:sendMessage("Hello")

-- Wrong - Dot syntax doesn't work for instance methods
Bukkit.broadcastMessage("Hello")  -- Will error
player.sendMessage("Hello")       -- Will error
```

2. **1-indexed arrays in Lua:**
```lua
-- Lua arrays start at 1, not 0
local args = {"a", "b", "c"}
print(args[1])  -- Prints "a"
print(args[0])  -- Prints nil
```

3. **Nil checks:**
```lua
-- Check for nil properly
if player == nil then
    -- Handle nil case
end

-- Or use Lua's truthiness
if not player then
    -- Handle nil case
end
```

### Performance Issues

**Symptoms:** Server lag when scripts run

**Common Causes:**
1. Expensive operations in frequent events
2. Infinite loops
3. Too many entities spawned

**Solutions:**
```lua
-- Avoid expensive operations in PlayerMoveEvent
on("PlayerMoveEvent", function(event)
    -- Bad: Complex calculations every move
    -- Good: Use throttling
    if math.random(100) == 1 then  -- Only process 1% of moves
        -- Do something
    end
end)

-- Limit entity spawning
for i = 1, math.min(amount, 10) do
    -- Spawn maximum 10 entities
end
```

### Memory Issues

**Symptoms:** OutOfMemoryError or gradual memory increase

**Causes:**
1. Storing references to objects that should be garbage collected
2. Creating too many objects without cleanup

**Solutions:**
```lua
-- Clear references when no longer needed
local playerData = {}
on("PlayerQuitEvent", function(event)
    local id = event:getPlayer():getUniqueId():toString()
    playerData[id] = nil  -- Clear data
end)

-- Use weak tables for temporary storage
setmetatable(playerData, {__mode = "v"})  -- Values are weak references
```

## Debugging Tips

### 1. Use the Logger API

```lua
-- Log important information
info("Script loaded")
warning("Something unusual happened")
error("Critical error occurred")
```

### 2. Use pcall for Error Handling

```lua
local success, result = pcall(function()
    -- Risky code here
    return someFunction()
end)

if not success then
    error("Function failed: " .. tostring(result))
end
```

### 3. Check Object Types

```lua
-- Check if object is expected type
local Player = Class("org.bukkit.entity.Player")
if not Player:isInstance(player) then
    sender:sendMessage("This command requires a player!")
    return
end

-- Check if value is nil
if not world then
    error("World not found")
end
```

### 4. Use Debug Mode

```lua
local DEBUG = false

function debug(message)
    if DEBUG then
        info("[DEBUG] " .. message)
    end
end

-- Toggle with command
registerCommand("debug", "plugin.debug", function(sender, args)
    DEBUG = not DEBUG
    sender:sendMessage("Debug mode: " .. (DEBUG and "ON" or "OFF"))
end)
```

## Getting Help

If you encounter issues not covered here:

1. Check the server console for error messages
2. Enable debug logging in your scripts
3. Test with minimal code to isolate the problem
4. Check the plugin documentation for correct API usage
5. Verify your Lua syntax using a Lua validator

## Best Practices Recap

1. Always validate inputs before using them
2. Use proper error handling with pcall
3. Log important actions for debugging
4. Use permissions for sensitive operations
5. Consider performance impact of your code
6. Test scripts in a development environment first
7. Keep backups of working scripts
8. Use descriptive variable and function names
9. Comment complex logic