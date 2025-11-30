package me.touchie771.minecraftLuaScripting.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class PlayerApi {

    public static class GetPlayer extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            Player player = Bukkit.getPlayer(arg.checkjstring());
            if (player == null) return LuaValue.NIL;
            return CoerceJavaToLua.coerce(player);
        }
    }

    public static class SendMessage extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue playerVal, LuaValue messageVal) {
            Player player = (Player) playerVal.checkuserdata(Player.class);
            String message = messageVal.checkjstring();
            player.sendMessage(message);
            return LuaValue.NONE;
        }
    }

    public static class Teleport extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            Player player = (Player) args.checkuserdata(1, Player.class);
            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            player.teleport(new Location(player.getWorld(), x, y, z));
            return LuaValue.NONE;
        }
    }

    public static class GetHealth extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            Player player = (Player) arg.checkuserdata(Player.class);
            return LuaValue.valueOf(player.getHealth());
        }
    }

    public static class SetHealth extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue playerVal, LuaValue healthVal) {
            Player player = (Player) playerVal.checkuserdata(Player.class);
            double health = healthVal.checkdouble();
            player.setHealth(health);
            return LuaValue.NONE;
        }
    }

    public static class GetLocation extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            Player player = (Player) arg.checkuserdata(Player.class);
            Location loc = player.getLocation();
            LuaTable locationTable = new LuaTable();
            locationTable.set("x", LuaValue.valueOf(loc.getX()));
            locationTable.set("y", LuaValue.valueOf(loc.getY()));
            locationTable.set("z", LuaValue.valueOf(loc.getZ()));
            locationTable.set("yaw", LuaValue.valueOf(loc.getYaw()));
            locationTable.set("pitch", LuaValue.valueOf(loc.getPitch()));
            locationTable.set("world", LuaValue.valueOf(loc.getWorld().getName()));
            return locationTable;
        }
    }

    public static class Kick extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue playerVal, LuaValue reasonVal) {
            Player player = (Player) playerVal.checkuserdata(Player.class);
            String reason = reasonVal.optjstring("Kicked from server");
            player.kick(Component.text(reason));
            return LuaValue.NONE;
        }
    }
}