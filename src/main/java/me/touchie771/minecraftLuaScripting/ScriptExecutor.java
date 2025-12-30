package me.touchie771.minecraftLuaScripting;

import me.touchie771.minecraftLuaScripting.api.EntityApi;
import me.touchie771.minecraftLuaScripting.api.InventoryApi;
import me.touchie771.minecraftLuaScripting.api.LoggerApi;
import me.touchie771.minecraftLuaScripting.api.PlayerApi;
import me.touchie771.minecraftLuaScripting.api.SchedulerApi;
import me.touchie771.minecraftLuaScripting.api.ServerApi;
import me.touchie771.minecraftLuaScripting.api.WorldApi;
import me.touchie771.minecraftLuaScripting.commandHandlers.CommandRegister;
import me.touchie771.minecraftLuaScripting.eventHandlers.EventListener;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.OneArgFunction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScriptExecutor {

    private static final File scriptsFolder = new File("LuaScripts");
    private static Globals globals = JsePlatform.standardGlobals();
    private static EventListener eventListener;

    public static void cleanup(MinecraftLuaScripting plugin) {
        try {
            CommandRegister.clearLuaCommands(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clear Lua commands: " + e);
        }

        try {
            SchedulerApi.cancelAllTasks(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to cancel Lua tasks: " + e);
        }

        try {
            if (eventListener != null) {
                org.bukkit.event.HandlerList.unregisterAll(eventListener);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unregister Lua event listeners: " + e);
        } finally {
            eventListener = null;
        }

        globals = JsePlatform.standardGlobals();
    }

    public static void reloadAll(MinecraftLuaScripting plugin) {
        cleanup(plugin);
        setup(plugin);
        executeScripts();
    }

    public static void setup(MinecraftLuaScripting plugin) {
        loadApi(plugin);

        if (!scriptsFolder.exists()) {
            if (scriptsFolder.mkdirs()) {
                plugin.getLogger().info("Created scripts folder");
            }
            else {
                plugin.getLogger().severe("Failed to create scripts folder");
                plugin.onDisable();
            }
        }

        saveExampleScripts(plugin);
    }

    public static void executeScripts() {
        for (File script : Objects.requireNonNull(scriptsFolder.listFiles())) {
            if (script.getName().endsWith(".lua")) {
                globals.loadfile(script.toPath().toString()).call();
            }
        }
    }

    private static final class JavaClassBinder extends OneArgFunction {
        private final Map<String, LuaValue> cache = new HashMap<>();

        @Override
        public LuaValue call(LuaValue classNameVal) {
            String className = classNameVal.checkjstring();
            LuaValue cached = cache.get(className);
            if (cached != null) {
                return cached;
            }

            try {
                Class<?> clazz = Class.forName(className);
                LuaValue coerced = CoerceJavaToLua.coerce(clazz);
                cache.put(className, coerced);
                return coerced;
            } catch (ClassNotFoundException e) {
                return LuaValue.error("Class not found: " + className);
            }
        }
    }

    private static void saveExampleScripts(MinecraftLuaScripting plugin) {
        try {
            File exampleScript = new File(scriptsFolder, "example.lua");
            if (!exampleScript.exists()) {
                try (InputStream inputStream = plugin.getResource("example.lua")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, exampleScript.toPath());
                        plugin.getLogger().info("Saved example.lua to scripts folder");
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save example script: " + e.getMessage());
        }
    }

    private static void loadApi(MinecraftLuaScripting plugin) {
        // Expose Java Classes (for static access and enums)
        globals.set("Bukkit", CoerceJavaToLua.coerce(org.bukkit.Bukkit.class));
        globals.set("Material", CoerceJavaToLua.coerce(org.bukkit.Material.class));
        globals.set("EntityType", CoerceJavaToLua.coerce(org.bukkit.entity.EntityType.class));
        globals.set("GameMode", CoerceJavaToLua.coerce(org.bukkit.GameMode.class));

        globals.set("Class", new JavaClassBinder());
        globals.set("plugin", CoerceJavaToLua.coerce(plugin));

        // Player API (Factories & Helpers)
        globals.set("getPlayer", new PlayerApi.GetPlayer());
        globals.set("teleport", new PlayerApi.Teleport()); // Helper for x,y,z
        globals.set("kick", new PlayerApi.Kick()); // Helper for Component text

        // Server API (Helpers)
        globals.set("executeAs", new ServerApi.ExecuteAs());
        globals.set("getOnlinePlayers", new ServerApi.GetOnlinePlayers()); // Returns table of names (easier than Collection)

        // World API
        globals.set("getWorld", new WorldApi.GetWorld());
        globals.set("createExplosion", new WorldApi.CreateExplosion()); // Helper for x,y,z

        // Entity API
        globals.set("spawnEntity", new EntityApi.SpawnEntity()); // Helper for Location/Type

        // Inventory API
        globals.set("getInventory", new InventoryApi.GetInventory());
        globals.set("addItem", new InventoryApi.AddItem()); // Helper for Material/ItemStack

        // Logger API
        globals.set("info", new LoggerApi.Info(plugin.getLogger()));
        globals.set("warning", new LoggerApi.Warning(plugin.getLogger()));
        globals.set("error", new LoggerApi.Error(plugin.getLogger()));

        // Event API
        eventListener = new EventListener(plugin);
        globals.set("on", eventListener.new On());

        // Scheduler API
        globals.set("runLater", new SchedulerApi.RunLater(plugin));
        globals.set("runRepeating", new SchedulerApi.RunRepeating(plugin));
        globals.set("runAsync", new SchedulerApi.RunAsync(plugin));
        globals.set("runAsyncLater", new SchedulerApi.RunAsyncLater(plugin));
        globals.set("cancelTask", new SchedulerApi.CancelTask(plugin));
        globals.set("cancelAllTasks", new SchedulerApi.CancelAllTasks(plugin));

        // Command API
        globals.set("registerCommand", new CommandRegister(plugin).new Register());
    }

    public static File getScriptsFolder() {
        return scriptsFolder;
    }
}