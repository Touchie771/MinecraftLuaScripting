# Minecraft Lua Scripting Plugin Documentation

This plugin allows you to write Lua scripts to interact with Minecraft server events and create custom commands.

## Global Variables

- `plugin` - The plugin instance (JavaPlugin)
- `Class(name)` - Returns a Java class by fully-qualified name (cached)
- `runLater(ticks, fn)` - Run a Lua callback on the main thread after `ticks`
- `runRepeating(delayTicks, periodTicks, fn)` - Run a Lua callback repeatedly on the main thread
- `runAsync(fn)` - Run a Lua callback asynchronously (**do not call Bukkit API async**)
- `runAsyncLater(ticks, fn)` - Run a Lua callback asynchronously after `ticks`
- `cancelTask(taskId)` - Cancel a scheduled task by id
- `cancelAllTasks(nil)` - Cancel all tasks created by scripts

`Class()` is intended for admin-only scripts and provides access to any class available on the server classpath.

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

-- Register a command
registerCommand("hello", "permission.hello", function(sender, args)
    sender:sendMessage("Hello, " .. sender:getName() .. "!")
end)
```

## Script Location

All Lua scripts should be placed in the `LuaScripts` folder in your server's root directory. Scripts are automatically loaded when the server starts or when using the `/luascript` command with the following subcommands:
- `reloadall` - Reload all scripts
- `list` - List all scripts found in the `LuaScripts` folder
- `run <script>` - Run a specific script
- `reload <script>` - Reload a specific script