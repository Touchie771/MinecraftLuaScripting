package me.touchie771.minecraftLuaScripting.eventHandlers;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {

    private final MinecraftLuaScripting plugin;
    private final Map<String, Class<? extends Event>> eventCache = new HashMap<>();

    public EventListener(MinecraftLuaScripting plugin) {
        this.plugin = plugin;
    }

    public class On extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue eventNameVal, LuaValue callbackVal) {
            String eventName = eventNameVal.checkjstring();
            if (!callbackVal.isfunction()) {
                return LuaValue.error("Callback must be a function");
            }

            Class<? extends Event> eventClass = getEventClass(eventName);
            if (eventClass == null) {
                plugin.getLogger().warning("Could not find event class for: " + eventName);
                return LuaValue.FALSE;
            }

            Bukkit.getPluginManager().registerEvent(eventClass, EventListener.this, EventPriority.NORMAL, (listener, event) -> {
                if (eventClass.isInstance(event)) {
                    try {
                        callbackVal.call(CoerceJavaToLua.coerce(event));
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error in Lua event listener for " + eventName + ": " + e.getMessage());
                    }
                }
            }, plugin);

            return LuaValue.TRUE;
        }
    }

    private Class<? extends Event> getEventClass(String name) {
        if (eventCache.containsKey(name)) return eventCache.get(name);

        String[] packages = {
            "org.bukkit.event.",
            "org.bukkit.event.player.",
            "org.bukkit.event.entity.",
            "org.bukkit.event.block.",
            "org.bukkit.event.world.",
            "org.bukkit.event.inventory.",
            "org.bukkit.event.server.",
            "org.bukkit.event.vehicle.",
            "org.bukkit.event.weather.",
            "org.bukkit.event.hanging.",
            "org.bukkit.event.enchantment.",
            "org.bukkit.event.raid."
        };

        for (String pkg : packages) {
            try {
                Class<?> clazz = Class.forName(pkg + name);
                if (Event.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) clazz;
                    eventCache.put(name, eventClass);
                    return eventClass;
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }
}