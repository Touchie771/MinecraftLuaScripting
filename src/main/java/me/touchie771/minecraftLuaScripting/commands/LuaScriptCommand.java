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
import java.util.List;

public record LuaScriptCommand(MinecraftLuaScripting plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /luascript <reloadall|list>");
            return true;
        }
        if (!sender.hasPermission("luascript.admin")) return true;

        String sub = args[0].toLowerCase();

        if (sub.equals("reloadall")) {
            sender.sendMessage("§eReloading all scripts...");
            try {
                ScriptExecutor.reloadAll(plugin);
                sender.sendMessage("§aReloaded all scripts!");
            } catch (Exception e) {
                sender.sendMessage("§cFailed to reload scripts: " + e.getMessage());
            }
            return true;
        } else if (sub.equals("list")) {
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
        } else {
            sender.sendMessage("§cUnknown subcommand. Usage: /luascript <reloadall|list>");
        }
        return true;
    }

    @Override
    public @NotNull @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("reloadall", "list");
        }
        return List.of();
    }
}