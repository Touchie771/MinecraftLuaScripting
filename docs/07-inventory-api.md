# Inventory API

The Inventory API provides functions for managing player and entity inventories.

## Available Functions

### getInventory(holder)

Retrieves the inventory of a player or entity.

```lua
local player = getPlayer("Steve")
if player then
    local inventory = getInventory(player)
    -- Now you can manipulate the inventory
end
```

**Parameters:**
- `holder` (Player): The player or entity whose inventory to get

**Returns:**
- Inventory object or `nil` if holder doesn't have an inventory

### addItem(inventory, material, amount)

Adds an item to an inventory.

```lua
local player = getPlayer("Steve")
if player then
    local inventory = getInventory(player)
    addItem(inventory, "DIAMOND", 5)
end
```

**Parameters:**
- `inventory` (Inventory): The inventory to add item to
- `material` (string): Material name (case-insensitive)
- `amount` (number, optional): Amount to add (default: 1)

**Returns:**
- `true` if successful, error message if material is invalid

## Inventory Object Methods

When you have an Inventory object, you can use these methods:

### Information Methods

```lua
local inventory = getInventory(player)

-- Get inventory size
local size = inventory:getSize()

-- Get item at specific slot
local item = inventory:getItem(0)  -- First slot

-- Check if inventory is empty
local empty = inventory:isEmpty()

-- Get first empty slot
local firstEmpty = inventory:firstEmpty()

-- Get all items
local contents = inventory:getContents()
```

### Item Manipulation

```lua
local inventory = getInventory(player)

-- Set item in slot
local itemStack = ItemStack(Material.DIAMOND_SWORD, 1)
inventory:setItem(0, itemStack)

-- Add item stack
local added = inventory:addItem(itemStack)

-- Remove item
inventory:removeItem(itemStack)

-- Clear entire inventory
inventory:clear()

-- Clear specific slot
inventory:clear(0)
```

### Item Stack Methods

When working with ItemStack objects:

```lua
local item = inventory:getItem(0)

-- Get item type
local material = item:getType():name()

-- Get item amount
local amount = item:getAmount()

-- Set item amount
item:setAmount(10)

-- Get item display name
local name = item:getItemMeta():getDisplayName()

-- Check if item has lore
local hasLore = item:getItemMeta():hasLore()

-- Get item lore
local lore = item:getItemMeta():getLore()
```

## Usage Examples

### Give Item Command

```lua
registerCommand("give", "plugin.give", function(sender, args)
    if not args[1] or not args[2] then
        sender:sendMessage("Usage: /give <player> <item> [amount]")
        return
    end
    
    local target = getPlayer(args[1])
    if not target then
        sender:sendMessage("§cPlayer not found: " .. args[1])
        return
    end
    
    local materialName = args[2]:upper()
    local amount = tonumber(args[3]) or 1
    
    local inventory = getInventory(target)
    if addItem(inventory, materialName, amount) then
        sender:sendMessage("§aGave " .. amount .. " " .. materialName .. " to " .. target:getName())
        target:sendMessage("§aYou received " .. amount .. " " .. materialName .. "!")
    else
        sender:sendMessage("§cInvalid item: " .. materialName)
    end
end)
```

### Kit System

```lua
local kits = {
    starter = {
        {item = "WOODEN_SWORD", amount = 1},
        {item = "BREAD", amount = 10},
        {item = "WOODEN_PICKAXE", amount = 1},
        {item = "TORCH", amount = 16}
    },
    vip = {
        {item = "DIAMOND_SWORD", amount = 1},
        {item = "DIAMOND_PICKAXE", amount = 1},
        {item = "DIAMOND_ARMOR", amount = 4},
        {item = "GOLDEN_APPLE", amount = 5}
    }
}

registerCommand("kit", nil, function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local kitName = args[1] or "starter"
    local kit = kits[kitName]
    
    if not kit then
        sender:sendMessage("§cKit not found. Available kits: starter, vip")
        return
    end
    
    if kitName == "vip" and not sender:hasPermission("kit.vip") then
        sender:sendMessage("§cYou don't have permission for this kit!")
        return
    end
    
    local inventory = getInventory(sender)
    
    for _, itemData in ipairs(kit) do
        addItem(inventory, itemData.item, itemData.amount)
    end
    
    sender:sendMessage("§aYou received the " .. kitName .. " kit!")
end)
```

### Inventory Check Command

```lua
registerCommand("invsee", "plugin.invsee", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    if not args[1] then
        sender:sendMessage("Usage: /invsee <player>")
        return
    end
    
    local target = getPlayer(args[1])
    if not target then
        sender:sendMessage("§cPlayer not found: " .. args[1])
        return
    end
    
    local inventory = getInventory(target)
    local viewer = sender
    
    viewer:sendMessage("§6=== " .. target:getName() .. "'s Inventory ===")
    
    for i = 0, inventory:getSize() - 1 do
        local item = inventory:getItem(i)
        if item and not item:getType():name():equals("AIR") then
            viewer:sendMessage("§fSlot " .. i .. ": " .. item:getType():name() .. " x" .. item:getAmount())
        end
    end
end)
```

### Clear Inventory Command

```lua
registerCommand("clearinv", "plugin.clearinv", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local target = sender
    
    if args[1] then
        if sender:hasPermission("plugin.clearinv.others") then
            target = getPlayer(args[1])
            if not target then
                sender:sendMessage("§cPlayer not found: " .. args[1])
                return
            end
        else
            sender:sendMessage("§cYou don't have permission to clear other players' inventories!")
            return
        end
    end
    
    local inventory = getInventory(target)
    inventory:clear()
    
    if target == sender then
        sender:sendMessage("§aYour inventory has been cleared!")
    else
        sender:sendMessage("§aCleared " .. target:getName() .. "'s inventory!")
        target:sendMessage("§cYour inventory has been cleared by " .. sender:getName())
    end
end)
```

### Repair Command

```lua
registerCommand("repair", "plugin.repair", function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local inventory = getInventory(player)
    local item = inventory:getItemInMainHand()
    
    if item:getType():name():equals("AIR") then
        player:sendMessage("§cYou must hold an item to repair!")
        return
    end
    
    local meta = item:getItemMeta()
    if not meta or not meta:hasDamage() or meta:getDamage() == 0 then
        player:sendMessage("§cThis item is not damaged!")
        return
    end
    
    meta:setDamage(0)
    item:setItemMeta(meta)
    inventory:setItemInMainHand(item)
    
    player:sendMessage("§aYour item has been repaired!")
end)
```

### Sort Inventory Command

```lua
registerCommand("sortinv", nil, function(sender, args)
    local Player = Class("org.bukkit.entity.Player")
    if not Player:isInstance(sender) then
        sender:sendMessage("This command can only be used by players.")
        return
    end
    
    local player = sender
    local inventory = getInventory(player)
    
    -- Get all items
    local items = {}
    for i = 0, inventory:getSize() - 1 do
        local item = inventory:getItem(i)
        if item and not item:getType():name():equals("AIR") then
            table.insert(items, item)
        end
    end
    
    -- Sort items by type
    table.sort(items, function(a, b)
        return a:getType():name() < b:getType():name()
    end)
    
    -- Clear inventory
    inventory:clear()
    
    -- Add sorted items back
    for _, item in ipairs(items) do
        inventory:addItem(item)
    end
    
    player:sendMessage("§aInventory sorted!")
end)
```

## Material Types

Common materials you can use:
- Tools: `WOODEN_SWORD`, `STONE_SWORD`, `IRON_SWORD`, `GOLDEN_SWORD`, `DIAMOND_SWORD`, `NETHERITE_SWORD`
- Armor: `LEATHER_HELMET`, `IRON_CHESTPLATE`, `DIAMOND_LEGGINGS`, `NETHERITE_BOOTS`
- Blocks: `STONE`, `DIRT`, `WOOD`, `COBBLESTONE`, `BRICKS`, `GLASS`
- Food: `BREAD`, `APPLE`, `COOKED_BEEF`, `GOLDEN_APPLE`, `CAKE`
- Redstone: `REDSTONE`, `REPEATER`, `PISTON`, `HOPPER`
- Valuable: `DIAMOND`, `EMERALD`, `GOLD_INGOT`, `IRON_INGOT`

## Tips and Best Practices

1. Always check if a player exists before getting their inventory
2. Use proper material names (they're case-insensitive but best to use uppercase)
3. Consider inventory space when adding items
4. Use permissions for inventory manipulation commands
5. Be careful with valuable items in commands
6. Check if items are damaged before repairing