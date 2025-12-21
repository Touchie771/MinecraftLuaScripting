package me.touchie771.minecraftLuaScripting.commandHandlers;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CommandRegister {

    private final MinecraftLuaScripting plugin;
    private CommandMap commandMap;

    public CommandRegister(MinecraftLuaScripting plugin) {
        this.plugin = plugin;
        setupCommandMap();
    }

    private void setupCommandMap() {
        try {
            this.commandMap = resolveCommandMap();
            if (this.commandMap == null) {
                plugin.getLogger().severe("Failed to get CommandMap: resolved to null");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get CommandMap: " + e);
        }
    }

    private CommandMap resolveCommandMap() {
        // 1) Try CraftServer#getCommandMap() (public on CraftBukkit/Paper)
        try {
            Method getCommandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object result = getCommandMap.invoke(Bukkit.getServer());
            if (result instanceof CommandMap cm) {
                return cm;
            }
        } catch (Exception ignored) {
            // fallback
        }

        // 2) Try field "commandMap" on server class hierarchy
        try {
            Field f = findField(Bukkit.getServer().getClass(), "commandMap");
            if (f != null) {
                f.setAccessible(true);
                Object result = f.get(Bukkit.getServer());
                if (result instanceof CommandMap cm) {
                    return cm;
                }
            }
        } catch (Exception ignored) {
            // fallback
        }

        // 3) Try SimplePluginManager#commandMap
        try {
            PluginManager pm = Bukkit.getPluginManager();
            if (pm instanceof SimplePluginManager) {
                Field f = findField(pm.getClass(), "commandMap");
                if (f != null) {
                    f.setAccessible(true);
                    Object result = f.get(pm);
                    if (result instanceof CommandMap cm) {
                        return cm;
                    }
                }
            }
        } catch (Exception ignored) {
            // fallback
        }

        return null;
    }

    private static Field findField(Class<?> startClass, String fieldName) {
        Class<?> c = startClass;
        while (c != null) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
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
                LuaTable argsTable = new LuaTable();
                for (int i = 0; i < args.length; i++) {
                    argsTable.set(i + 1, LuaValue.valueOf(args[i]));
                }
                LuaValue argsVal = argsTable;
                callback.call(senderVal, argsVal);
            } catch (Exception e) {
                sender.sendMessage("§cError executing Lua command: " + e.getMessage());
                plugin.getLogger().severe("Error executing Lua command '" + getName() + "': " + e.getMessage());
            }
            return true;
        }
    }
}