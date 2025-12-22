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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandRegister {

    private static final Map<String, Set<String>> LUA_COMMANDS_BY_PLUGIN = new HashMap<>();

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

    public static void clearLuaCommands(MinecraftLuaScripting plugin) {
        String pluginKey = plugin.getName();
        Set<String> names = LUA_COMMANDS_BY_PLUGIN.remove(pluginKey);
        if (names == null || names.isEmpty()) {
            return;
        }

        CommandRegister resolver = new CommandRegister(plugin);
        CommandMap cm = resolver.commandMap;
        if (cm == null) {
            plugin.getLogger().warning("Cannot clear Lua commands: CommandMap not initialized");
            return;
        }

        Map<String, Command> knownCommands = resolveKnownCommands(cm);
        for (String name : names) {
            try {
                unregisterCommand(cm, knownCommands, pluginKey, name);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister Lua command '" + name + "': " + e);
            }
        }
    }

    private static void unregisterCommand(CommandMap cm, Map<String, Command> knownCommands, String pluginKey, String name) {
        if (knownCommands == null) {
            return;
        }

        Command cmd = knownCommands.get(name);
        if (cmd != null) {
            cmd.unregister(cm);
        }

        Command cmdNamespaced = knownCommands.get(pluginKey.toLowerCase() + ":" + name);
        if (cmdNamespaced != null) {
            cmdNamespaced.unregister(cm);
        }

        knownCommands.remove(name);
        knownCommands.remove(pluginKey.toLowerCase() + ":" + name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> resolveKnownCommands(CommandMap commandMap) {
        try {
            // Usually org.bukkit.command.SimpleCommandMap
            Field f = findField(commandMap.getClass(), "knownCommands");
            if (f == null) {
                return null;
            }
            f.setAccessible(true);
            Object val = f.get(commandMap);
            if (val instanceof Map) {
                return (Map<String, Command>) val;
            }
        } catch (Exception ignored) {
            // ignore
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

            // If a previous Lua reload already registered this name, replace it.
            Map<String, Command> knownCommands = resolveKnownCommands(commandMap);
            unregisterCommand(commandMap, knownCommands, plugin.getName(), commandName);

            LuaCommand command = new LuaCommand(commandName, permission, callbackVal);
            commandMap.register(plugin.getName(), command);
            LUA_COMMANDS_BY_PLUGIN
                    .computeIfAbsent(plugin.getName(), k -> new HashSet<>())
                    .add(commandName);
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