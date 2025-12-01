package me.touchie771.minecraftLuaScripting.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class EntityApi {

    public static class SpawnEntity extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            World world = (World) args.checkuserdata(1, World.class);
            double x = args.checkdouble(2);
            double y = args.checkdouble(3);
            double z = args.checkdouble(4);
            String typeName = args.checkjstring(5).toUpperCase();
            
            try {
                EntityType type = EntityType.valueOf(typeName);
                Entity entity = world.spawnEntity(new Location(world, x, y, z), type);
                return CoerceJavaToLua.coerce(entity);
            } catch (IllegalArgumentException e) {
                return LuaValue.error("Invalid entity type: " + typeName);
            }
        }
    }

    public static class RemoveEntity extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue entityVal) {
            Entity entity = (Entity) entityVal.checkuserdata(Entity.class);
            entity.remove();
            return LuaValue.NONE;
        }
    }

    public static class GetEntityId extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue entityVal) {
            Entity entity = (Entity) entityVal.checkuserdata(Entity.class);
            return LuaValue.valueOf(entity.getEntityId());
        }
    }
}