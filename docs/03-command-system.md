# Command System

The command system allows you to register custom commands with optional permissions.

## Registering Commands

Use the `registerCommand()` function to register a new command:

```lua
registerCommand(name, permission, callback)
```

### Parameters

- `name` (string): The command name (without the `/`)
- `permission` (string): Required permission to use the command (can be `nil` for no permission)
- `callback` (function): Function to execute when command is used

### Callback Function Signature

```lua
function(sender, args)
    -- sender: CommandSender (player or console)
    -- args: table of string arguments
end
```

## Basic Command Examples

### Simple Command

```lua
registerCommand("ping", nil, function(sender, args)
    sender:sendMessage("Pong!")
end)
```

### Command with Permission

```lua
registerCommand("fly", "plugin.fly", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if Player:isInstance(sender) then
        local player = sender
        player:setAllowFlight(true)
        player:sendMessage("Fly mode enabled!")
    else
        sender:sendMessage("This command can only be used by players.")
    end
end)
```

### Command with Arguments

```lua
registerCommand("give", "plugin.give", function(sender, args)
    if not args[1] or not args[2] then
        sender:sendMessage("Usage: /give <player> <item> [amount]")
        return
    end
    
    local target = getPlayer(args[1])
    if not target then
        sender:sendMessage("Player not found: " .. args[1])
        return
    end
    
    local materialName = args[2]:upper()
    local amount = tonumber(args[3]) or 1
    
    local inventory = getInventory(target)
    if addItem(inventory, materialName, amount) then
        sender:sendMessage("Gave " .. amount .. " " .. materialName .. " to " .. target:getName())
    else
        sender:sendMessage("Invalid item: " .. materialName)
    end
end)
```

## Command Features

### Permission System

Commands can require specific permissions:
```lua
-- Only players with 'admin.godmode' can use this
registerCommand("godmode", "admin.godmode", function(sender, args)
    -- Command logic
end)
```

### Console Support

Commands work for both players and console:
```lua
registerCommand("status", nil, function(sender, args)
    local onlineCount = #getOnlinePlayers()
    sender:sendMessage("There are " .. onlineCount .. " players online.")
end)
```

## Advanced Examples

### Subcommand System

```lua
registerCommand("shop", nil, function(sender, args)
    local subcommand = args[1]
    
    if subcommand == "buy" then
        -- Handle buy command
        sender:sendMessage("Shop buy functionality")
    elseif subcommand == "sell" then
        -- Handle sell command
        sender:sendMessage("Shop sell functionality")
    elseif subcommand == "list" then
        -- Handle list command
        sender:sendMessage("Available items: diamond, gold, iron")
    else
        sender:sendMessage("Usage: /shop <buy|sell|list>")
    end
end)
```

### Dynamic Command Registration

```lua
-- Register commands from a configuration table
local commands = {
    {name = "spawn", perm = nil},
    {name = "home", perm = "user.home"},
    {name = "warp", perm = "user.warp"}
}

for _, cmd in ipairs(commands) do
    registerCommand(cmd.name, cmd.perm, function(sender, args)
        sender:sendMessage("Executing " .. cmd.name .. " command")
    end)
end
```

## Error Handling

Command errors are caught and displayed to the user:
```lua
registerCommand("test", nil, function(sender, args)
    -- This error will be shown to the player
    local result = someUndefinedFunction()
end)
```

## Best Practices

1. Always validate arguments in your commands
2. Use descriptive command names
3. Set appropriate permissions for sensitive commands
4. Provide helpful usage messages when arguments are missing
5. Check if the sender is a player when using player-specific methods

## Command Limitations

- Commands are registered at server startup or reload
- Command names cannot contain spaces
- Commands with the same name will override previous ones