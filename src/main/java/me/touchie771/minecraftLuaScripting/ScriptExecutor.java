package me.touchie771.minecraftLuaScripting;

import me.touchie771.minecraftLuaScripting.api.EntityApi;
import me.touchie771.minecraftLuaScripting.api.InventoryApi;
import me.touchie771.minecraftLuaScripting.api.LoggerApi;
import me.touchie771.minecraftLuaScripting.api.PlayerApi;
import me.touchie771.minecraftLuaScripting.api.ServerApi;
import me.touchie771.minecraftLuaScripting.api.WorldApi;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class ScriptExecutor {

    private static final File scriptsFolder = new File("LuaScripts");
    private static final Globals globals = JsePlatform.standardGlobals();

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
        // Player API
        globals.set("getPlayer", new PlayerApi.GetPlayer());
        globals.set("sendMessage", new PlayerApi.SendMessage());
        globals.set("teleport", new PlayerApi.Teleport());
        globals.set("getHealth", new PlayerApi.GetHealth());
        globals.set("setHealth", new PlayerApi.SetHealth());
        globals.set("getLocation", new PlayerApi.GetLocation());
        globals.set("kick", new PlayerApi.Kick());

        // Logger API
        globals.set("info", new LoggerApi.Info(plugin.getLogger()));
        globals.set("warning", new LoggerApi.Warning(plugin.getLogger()));
        globals.set("error", new LoggerApi.Error(plugin.getLogger()));

        // Server API
        globals.set("broadcast", new ServerApi.Broadcast());
        globals.set("executeCommand", new ServerApi.ExecuteConsoleCommand());
        globals.set("executeAs", new ServerApi.ExecuteAs());
        globals.set("getOnlinePlayers", new ServerApi.GetOnlinePlayers());
        globals.set("setWhitelist", new ServerApi.SetWhitelist());
        globals.set("getMaxPlayers", new ServerApi.GetMaxPlayers());

        // World API
        globals.set("getWorld", new WorldApi.GetWorld());
        globals.set("setTime", new WorldApi.SetTime());
        globals.set("getTime", new WorldApi.GetTime());
        globals.set("createExplosion", new WorldApi.CreateExplosion());
        globals.set("setStorm", new WorldApi.SetStorm());

        // Entity API
        globals.set("spawnEntity", new EntityApi.SpawnEntity());
        globals.set("removeEntity", new EntityApi.RemoveEntity());
        globals.set("getEntityId", new EntityApi.GetEntityId());

        // Inventory API
        globals.set("getInventory", new InventoryApi.GetInventory());
        globals.set("addItem", new InventoryApi.AddItem());
        globals.set("clearInventory", new InventoryApi.ClearInventory());
    }
}