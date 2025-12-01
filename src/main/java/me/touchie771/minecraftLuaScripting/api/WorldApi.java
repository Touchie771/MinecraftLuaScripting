package me.touchie771.minecraftLuaScripting.api;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class WorldApi {

    public static class GetWorld extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue name) {
            World world = Bukkit.getWorld(name.checkjstring());
            if (world == null) return LuaValue.NIL;
            return CoerceJavaToLua.coerce(world);
        }
    }

    public static class SetTime extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue worldVal, LuaValue timeVal) {
            World world = (World) worldVal.checkuserdata(World.class);
            world.setTime(timeVal.checklong());
            return LuaValue.NONE;
        }
    }

    public static class GetTime extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue worldVal) {
            World world = (World) worldVal.checkuserdata(World.class);
            return LuaValue.valueOf(world.getTime());
        }
    }

    public static class CreateExplosion extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            World world = (World) args.checkuserdata(1, World.class);
            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            float power = (float) args.checkdouble(5);
            world.createExplosion(x, y, z, power);
            return LuaValue.NONE;
        }
    }
    
    public static class SetStorm extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue worldVal, LuaValue stormVal) {
            World world = (World) worldVal.checkuserdata(World.class);
            world.setStorm(stormVal.checkboolean());
            return LuaValue.NONE;
        }
    }
}