package me.touchie771.minecraftLuaScripting.commandHandlers;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;

public class CommandRegister {

    private final MinecraftLuaScripting plugin;
    private CommandMap commandMap;

    public CommandRegister(MinecraftLuaScripting plugin) {
        this.plugin = plugin;
        setupCommandMap();
    }

    private void setupCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            this.commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get CommandMap: " + e.getMessage());
        }
    }

    public class Register extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue nameVal, LuaValue permissionVal, LuaValue callbackVal) {
            String commandName = nameVal.checkjstring();
            
            String permission = null;
            if (!permissionVal.isnil()) {
                permission = permissionVal.checkjstring();
            }
            
            if (!callbackVal.isfunction()) {
                return LuaValue.error("Callback must be a function");
            }

            if (commandMap == null) {
                return LuaValue.error("CommandMap not initialized");
            }

            LuaCommand command = new LuaCommand(commandName, permission, callbackVal);
            commandMap.register(plugin.getName(), command);
            plugin.getLogger().info("Registered command: " + commandName + " with permission: " + permission);
            
            return LuaValue.TRUE;
        }
    }

    private class LuaCommand extends Command {
        private final LuaValue callback;

        protected LuaCommand(String name, String permission, LuaValue callback) {
            super(name);
            this.callback = callback;
            if (permission != null && !permission.isEmpty()) {
                setPermission(permission);
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Lua command";
        }

        @Override
        public @NotNull String getUsage() {
            return "/" + getName();
        }

        @Override
        public boolean testPermission(@NotNull CommandSender target) {
            if (getPermission() == null || getPermission().isEmpty()) {
                return true; // No permission required
            }
            return target.hasPermission(getPermission());
        }

        @Override
        public boolean testPermissionSilent(@NotNull CommandSender target) {
            if (getPermission() == null || getPermission().isEmpty()) {
                return true; // No permission required
            }
            return target.hasPermission(getPermission());
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            try {
                LuaValue senderVal = CoerceJavaToLua.coerce(sender);
                LuaValue argsVal = CoerceJavaToLua.coerce(args);
                callback.call(senderVal, argsVal);
            } catch (Exception e) {
                sender.sendMessage("§cError executing Lua command: " + e.getMessage());
                plugin.getLogger().severe("Error executing Lua command '" + getName() + "': " + e.getMessage());
            }
            return true;
        }
    }
}