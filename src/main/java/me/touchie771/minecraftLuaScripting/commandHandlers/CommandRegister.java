package me.touchie771.minecraftLuaScripting.commandHandlers;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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

    public class Register extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue nameVal, LuaValue callbackVal) {
            String commandName = nameVal.checkjstring();
            if (!callbackVal.isfunction()) {
                return LuaValue.error("Callback must be a function");
            }

            if (commandMap == null) {
                return LuaValue.error("CommandMap not initialized");
            }

            LuaCommand command = new LuaCommand(commandName, callbackVal);
            commandMap.register(plugin.getName(), command);
            plugin.getLogger().info("Registered command: " + commandName);
            
            return LuaValue.TRUE;
        }
    }

    private class LuaCommand extends Command {
        private final LuaValue callback;

        protected LuaCommand(String name, LuaValue callback) {
            super(name);
            this.callback = callback;
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