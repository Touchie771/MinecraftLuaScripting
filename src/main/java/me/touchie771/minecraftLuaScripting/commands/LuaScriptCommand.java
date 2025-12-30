package me.touchie771.minecraftLuaScripting.commands;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import me.touchie771.minecraftLuaScripting.ScriptExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public record LuaScriptCommand(MinecraftLuaScripting plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /luascript <reloadall|list|run|reload> [script]");
            return true;
        }
        if (!sender.hasPermission("luascript.admin")) return true;

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reloadall" -> {
                sender.sendMessage("§eReloading all scripts...");
                try {
                    ScriptExecutor.reloadAll(plugin);
                    sender.sendMessage("§aReloaded all scripts!");
                } catch (Exception e) {
                    sender.sendMessage("§cFailed to reload scripts: " + e.getMessage());
                }
                return true;
            }
            case "list" -> {
                File folder = ScriptExecutor.getScriptsFolder();
                if (!folder.exists() || !folder.isDirectory()) {
                    sender.sendMessage("§cScripts folder not found.");
                    return true;
                }

                File[] files = folder.listFiles((dir, name) -> name.endsWith(".lua"));
                if (files == null || files.length == 0) {
                    sender.sendMessage("§eNo scripts found.");
                    return true;
                }

                sender.sendMessage("§aFound " + files.length + " scripts:");
                for (File file : files) {
                    sender.sendMessage("§7- §f" + file.getName());
                }
                return true;
            }
            case "run" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /luascript run <script>");
                    return true;
                }

                String scriptName = args[1];
                sender.sendMessage("§eRunning script: " + scriptName);

                try {
                    boolean success = ScriptExecutor.runScript(plugin, scriptName);
                    if (success) {
                        sender.sendMessage("§aSuccessfully executed script: " + scriptName);
                    } else {
                        sender.sendMessage("§cFailed to execute script. Check console for details.");
                    }
                } catch (Exception e) {
                    sender.sendMessage("§cUnexpected error: " + e.getMessage());
                }
                return true;
            }
            case "reload" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /luascript reload <script>");
                    return true;
                }

                String scriptName = args[1];
                sender.sendMessage("§eReloading script: " + scriptName);
                sender.sendMessage("§7Warning: This will clear ALL script resources, not just this script's!");

                try {
                    boolean success = ScriptExecutor.reloadScript(plugin, scriptName);
                    if (success) {
                        sender.sendMessage("§aSuccessfully reloaded script: " + scriptName);
                        sender.sendMessage("§7Note: Other scripts will need to be reloaded to restore their functionality.");
                    } else {
                        sender.sendMessage("§cFailed to reload script. Check console for details.");
                    }
                } catch (Exception e) {
                    sender.sendMessage("§cUnexpected error: " + e.getMessage());
                }
                return true;
            }
            default ->
                    sender.sendMessage("§cUnknown subcommand. Usage: /luascript <reloadall|list|run|reload> [script]");
        }
        return true;
    }

    @Override
    public @NotNull @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("reloadall", "list", "run", "reload");
        }
        
        // Tab complete script names for run and reload commands
        if (args.length == 2 && (args[0].equalsIgnoreCase("run") || args[0].equalsIgnoreCase("reload"))) {
            File folder = ScriptExecutor.getScriptsFolder();
            if (!folder.exists() || !folder.isDirectory()) {
                return List.of();
            }
            
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".lua"));
            if (files == null) {
                return List.of();
            }
            
            // Return script names without .lua extension for cleaner completion
            return Arrays.stream(files)
                    .map(File::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .map(name -> name.substring(0, name.length() - 4)) // Remove .lua
                    .sorted()
                    .toList();
        }
        
        return List.of();
    }
}