# Getting Started

## Installation

1. Download the MinecraftLuaScripting plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart the server or reload plugins
4. A `LuaScripts` folder will be created automatically in your server's root directory

## Your First Script

1. Create a new file in the `LuaScripts` folder with a `.lua` extension (e.g., `myscript.lua`)
2. Add your Lua code to the file
3. Use `/luascript reload` to reload all scripts or restart the server

## Basic Script Structure

```lua
-- This is a comment in Lua

-- All scripts have access to various global functions and APIs
-- See other documentation files for details

-- Example: Log a message when the script loads
info("My script has been loaded!")

-- Example: Listen to an event
on("PlayerJoinEvent", function(event)
    local player = event:getPlayer()
    player:sendMessage("Welcome to the server!")
end)
```

## Reloading Scripts

You can reload all Lua scripts without restarting the server using:
```
/luascript reload
```

This will:
- Unregister all existing event listeners
- Clear all registered commands
- Reload all `.lua` files from the `LuaScripts` folder

## Script Auto-loading

- Scripts are automatically loaded when the server starts
- Scripts are loaded in alphabetical order
- All files ending with `.lua` in the `LuaScripts` folder are executed
- If an error occurs in a script, it will be logged to the console

## Error Handling

- Lua errors are logged to the server console
- Event handler errors are caught and logged without stopping the plugin
- Command execution errors are shown to the player who used the command

## Best Practices

1. Use descriptive file names for your scripts
2. Organize related functionality in separate files
3. Use proper error handling in your Lua code
4. Test scripts on a development server before production
5. Keep backups of your scripts
6. Use comments to document complex logic