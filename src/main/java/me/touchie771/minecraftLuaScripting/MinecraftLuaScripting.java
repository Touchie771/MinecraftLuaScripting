package me.touchie771.minecraftLuaScripting;

import me.touchie771.minecraftLuaScripting.commands.LuaScriptCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MinecraftLuaScripting extends JavaPlugin {

    @Override
    public void onEnable() {
        ScriptExecutor.setup(this);
        ScriptExecutor.executeScripts();
        
        Objects.requireNonNull(getCommand("luascript")).setExecutor(new LuaScriptCommand(this));
        Objects.requireNonNull(getCommand("luascript")).setTabCompleter(new LuaScriptCommand(this));
    }

    @Override
    public void onDisable() {
        ScriptExecutor.cleanup(this);
    }
}