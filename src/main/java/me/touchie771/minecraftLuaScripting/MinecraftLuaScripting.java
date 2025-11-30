package me.touchie771.minecraftLuaScripting;

import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftLuaScripting extends JavaPlugin {

    @Override
    public void onEnable() {
        ScriptExecutor.setup(this);
        ScriptExecutor.executeScripts();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}