package me.touchie771.minecraftLuaScripting.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ServerApi {

    public static class Broadcast extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue message) {
            Bukkit.getServer().broadcast(Component.text(message.checkjstring()));
            return LuaValue.NONE;
        }
    }

    public static class ExecuteConsoleCommand extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue command) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.checkjstring());
            return LuaValue.NONE;
        }
    }

    public static class ExecuteAs extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue senderVal, LuaValue commandVal) {
            CommandSender sender = (CommandSender) senderVal.checkuserdata(CommandSender.class);
            String command = commandVal.checkjstring();
            Bukkit.dispatchCommand(sender, command);
            return LuaValue.NONE;
        }
    }

    public static class GetOnlinePlayers extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            LuaTable table = new LuaTable();
            int i = 1;
            for (Player player : Bukkit.getOnlinePlayers()) {
                table.set(i++, LuaValue.valueOf(player.getName()));
            }
            return table;
        }
    }

    public static class SetWhitelist extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue value) {
            Bukkit.setWhitelist(value.checkboolean());
            return LuaValue.NONE;
        }
    }

    public static class GetMaxPlayers extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(Bukkit.getMaxPlayers());
        }
    }
}