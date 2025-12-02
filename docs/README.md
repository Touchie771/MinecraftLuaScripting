# Minecraft Lua Scripting Plugin Documentation

This plugin allows you to write Lua scripts to interact with Minecraft server events and create custom commands.

## Table of Contents

- [Getting Started](01-getting-started.md)
- [Event System](02-event-system.md)
- [Command System](03-command-system.md)
- [Player API](04-player-api.md)
- [World API](05-world-api.md)
- [Entity API](06-entity-api.md)
- [Inventory API](07-inventory-api.md)
- [Server API](08-server-api.md)
- [Logger API](09-logger-api.md)
- [Bukkit Integration](10-bukkit-integration.md)

## Quick Example

```lua
-- Listen to block break events
on("BlockBreakEvent", function(event)
    local player = event:getPlayer()
    player:sendMessage("You broke a block!")
end)

-- Register a command with 15 second cooldown
registerCommand("hello", "permission.hello", 15, function(sender, args)
    sender:sendMessage("Hello, " .. sender:getName() .. "!")
end)
```

## Script Location

All Lua scripts should be placed in the `LuaScripts` folder in your server's root directory. Scripts are automatically loaded when the server starts or when using the `/luascript reload` command.