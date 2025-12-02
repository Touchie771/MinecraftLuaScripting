# Server API

The Server API provides functions for server-wide operations and command execution.

## Available Functions

### executeAs(sender, command)

Executes a command as a specific sender (player or console).

```lua
-- Execute command as console
local console = Bukkit:getConsoleSender()
executeAs(console, "say Hello from console!")

-- Execute command as player
local player = getPlayer("Steve")
if player then
    executeAs(player, "help")
end
```

**Parameters:**
- `sender` (CommandSender): The sender to execute command as
- `command` (string): The command to execute (without `/`)

### getOnlinePlayers()

Returns a table of all online player names.

```lua
local players = getOnlinePlayers()
for i, name in ipairs(players) do
    info("Online player: " .. name)
end
```

**Returns:**
- Table containing player names as strings

## Usage Examples

### Broadcast Command

```lua
registerCommand("broadcast", "plugin.broadcast", 30, function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /broadcast <message>")
        return
    end
    
    local message = table.concat(args, " ")
    executeAs(Bukkit:getConsoleSender(), "say " .. message)
    sender:sendMessage("§aMessage broadcasted!")
end)
```

### Online Players Command

```lua
registerCommand("online", nil, 5, function(sender, args)
    local players = getOnlinePlayers()
    local count = #players
    
    if count == 0 then
        sender:sendMessage("§cNo players are currently online.")
        return
    end
    
    sender:sendMessage("§6=== Online Players (" .. count .. ") ===")
    
    for i, name in ipairs(players) do
        local player = getPlayer(name)
        if player then
            local displayName = player:getDisplayName()
            local world = player:getWorld():getName()
            sender:sendMessage("§f- " .. displayName .. " §7(" .. world .. ")")
        end
    end
end)
```

### Server Status Command

```lua
registerCommand("serverinfo", nil, 10, function(sender, args)
    local server = Bukkit:getServer()
    local players = getOnlinePlayers()
    
    sender:sendMessage("§6=== Server Information ===")
    sender:sendMessage("§fServer Name: " .. server:getName())
    sender:sendMessage("§fServer Version: " .. server:getVersion())
    sender:sendMessage("§fBukkit Version: " .. server:getBukkitVersion())
    sender:sendMessage("§fOnline Players: " .. #players .. "/" .. server:getMaxPlayers())
    sender:sendMessage("§fServer Port: " .. server:getPort())
    sender:sendMessage("§fWorlds: " .. table.concat(getWorldNames(), ", "))
end)

function getWorldNames()
    local worlds = {}
    for _, world in ipairs(Bukkit:getWorlds()) do
        table.insert(worlds, world:getName())
    end
    return worlds
end
```

### Execute Command As Player

```lua
registerCommand("sudo", "plugin.sudo", 0, function(sender, args)
    if not args[1] or not args[2] then
        sender:sendMessage("Usage: /sudo <player> <command>")
        return
    end
    
    local target = getPlayer(args[1])
    if not target then
        sender:sendMessage("§cPlayer not found: " .. args[1])
        return
    end
    
    -- Remove the player name from arguments
    local command = table.concat(args, " ", 2)
    
    executeAs(target, command)
    sender:sendMessage("§aMade " .. target:getName() .. " execute: /" .. command)
end)
```

### Server Restart Command

```lua
registerCommand("restartserver", "plugin.restart", 60, function(sender, args)
    if not args[1] or args[1] ~= "confirm" then
        sender:sendMessage("§cThis will restart the server!")
        sender:sendMessage("§cUse /restartserver confirm to proceed.")
        return
    end
    
    sender:sendMessage("§eServer restarting in 10 seconds...")
    
    -- Countdown
    for i = 10, 1, -1 do
        Bukkit:broadcastMessage("§eServer restarting in " .. i .. " seconds!")
        -- In a real implementation, you'd need a proper delay mechanism
    end
    
    -- Execute restart command as console
    executeAs(Bukkit:getConsoleSender(), "restart")
end)
```

### Plugin Management

```lua
registerCommand("plugin", "plugin.manage", 0, function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /plugin <list|enable|disable|reload> [plugin]")
        return
    end
    
    local action = args[1]:lower()
    
    if action == "list" then
        local plugins = Bukkit:getPluginManager():getPlugins()
        sender:sendMessage("§6=== Installed Plugins ===")
        for _, plugin in ipairs(plugins) do
            local status = plugin:isEnabled() and "§aEnabled" or "§cDisabled"
            sender:sendMessage("§f- " .. plugin:getName() .. " " .. status)
        end
    elseif action == "enable" then
        if not args[2] then
            sender:sendMessage("§cPlease specify a plugin name.")
            return
        end
        
        local plugin = Bukkit:getPluginManager():getPlugin(args[2])
        if plugin then
            Bukkit:getPluginManager():enablePlugin(plugin)
            sender:sendMessage("§aPlugin " .. args[2] .. " enabled!")
        else
            sender:sendMessage("§cPlugin not found: " .. args[2])
        end
    elseif action == "disable" then
        if not args[2] then
            sender:sendMessage("§cPlease specify a plugin name.")
            return
        end
        
        local plugin = Bukkit:getPluginManager():getPlugin(args[2])
        if plugin then
            Bukkit:getPluginManager():disablePlugin(plugin)
            sender:sendMessage("§aPlugin " .. args[2] .. " disabled!")
        else
            sender:sendMessage("§cPlugin not found: " .. args[2])
        end
    elseif action == "reload" then
        if args[2] then
            -- Reload specific plugin
            local plugin = Bukkit:getPluginManager():getPlugin(args[2])
            if plugin then
                Bukkit:getPluginManager():disablePlugin(plugin)
                Bukkit:getPluginManager():enablePlugin(plugin)
                sender:sendMessage("§aPlugin " .. args[2] .. " reloaded!")
            else
                sender:sendMessage("§cPlugin not found: " .. args[2])
            end
        else
            -- Reload all plugins (requires console)
            executeAs(Bukkit:getConsoleSender(), "reload")
            sender:sendMessage("§aAll plugins reloaded!")
        end
    else
        sender:sendMessage("§cInvalid action. Use: list, enable, disable, or reload")
    end
end)
```

### Whitelist Management

```lua
registerCommand("whitelist", "plugin.whitelist", 0, function(sender, args)
    if not args[1] then
        sender:sendMessage("Usage: /whitelist <add|remove|on|off|list> [player]")
        return
    end
    
    local action = args[1]:lower()
    local server = Bukkit:getServer()
    
    if action == "on" then
        server:setWhitelist(true)
        sender:sendMessage("§aWhitelist enabled!")
    elseif action == "off" then
        server:setWhitelist(false)
        sender:sendMessage("§aWhitelist disabled!")
    elseif action == "add" then
        if not args[2] then
            sender:sendMessage("§cPlease specify a player name.")
            return
        end
        
        local success = server:getWhitelistedPlayers():add(Bukkit:getOfflinePlayer(args[2]))
        if success then
            sender:sendMessage("§aAdded " .. args[2] .. " to whitelist!")
        else
            sender:sendMessage("§cFailed to add player to whitelist.")
        end
    elseif action == "remove" then
        if not args[2] then
            sender:sendMessage("§cPlease specify a player name.")
            return
        end
        
        local success = server:getWhitelistedPlayers():remove(Bukkit:getOfflinePlayer(args[2]))
        if success then
            sender:sendMessage("§aRemoved " .. args[2] .. " from whitelist!")
        else
            sender:sendMessage("§cFailed to remove player from whitelist.")
        end
    elseif action == "list" then
        local whitelisted = server:getWhitelistedPlayers()
        sender:sendMessage("§6=== Whitelisted Players ===")
        for _, player in ipairs(whitelisted) do
            sender:sendMessage("§f- " .. player:getName())
        end
    else
        sender:sendMessage("§cInvalid action. Use: add, remove, on, off, or list")
    end
end)
```

## Server Properties

You can access various server properties:

```lua
local server = Bukkit:getServer()

-- Get server properties
local motd = server:getMotd()
local maxPlayers = server:getMaxPlayers()
local port = server:getPort()
local ip = server:getIp()
local allowNether = server:getAllowNether()
local allowEnd = server:getAllowEnd()
local difficulty = server:getDifficulty():name()
local viewDistance = server:getViewDistance()

-- Check server state
local isWhitelist = server:hasWhitelist()
local isHardcore = server:isHardcore()
```

## Tips and Best Practices

1. Use `executeAs()` with console for administrative commands
2. Always validate command arguments before execution
3. Use proper permissions for server management commands
4. Be careful with commands like `restart` and `reload`
5. Consider the impact of commands on all players
6. Log important server operations for auditing