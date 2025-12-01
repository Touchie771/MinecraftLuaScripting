package me.touchie771.minecraftLuaScripting.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class InventoryApi {

    public static class GetInventory extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue holderVal) {
            if (holderVal.isuserdata(Player.class)) {
                Player player = (Player) holderVal.checkuserdata(Player.class);
                return CoerceJavaToLua.coerce(player.getInventory());
            }
            return LuaValue.NIL;
        }
    }

    public static class AddItem extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            Inventory inventory = (Inventory) args.checkuserdata(1, Inventory.class);
            String materialName = args.checkjstring(2).toUpperCase();
            int amount = args.optint(3, 1);

            try {
                Material material = Material.valueOf(materialName);
                inventory.addItem(new ItemStack(material, amount));
                return LuaValue.TRUE;
            } catch (IllegalArgumentException e) {
                return LuaValue.error("Invalid material: " + materialName);
            }
        }
    }
}