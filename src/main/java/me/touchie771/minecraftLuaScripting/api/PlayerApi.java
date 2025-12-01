package me.touchie771.minecraftLuaScripting.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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