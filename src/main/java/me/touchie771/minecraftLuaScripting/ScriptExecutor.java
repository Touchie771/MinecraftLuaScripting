package me.touchie771.minecraftLuaScripting;

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
}