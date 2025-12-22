package me.touchie771.minecraftLuaScripting.api;

import me.touchie771.minecraftLuaScripting.MinecraftLuaScripting;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SchedulerApi {

    private static final Map<String, Set<Integer>> TASKS_BY_PLUGIN = new HashMap<>();

    private SchedulerApi() {
    }

    public static void cancelAllTasks(MinecraftLuaScripting plugin) {
        String key = plugin.getName();
        Set<Integer> ids = TASKS_BY_PLUGIN.remove(key);
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            Bukkit.getScheduler().cancelTask(id);
        }
    }

    private static void track(MinecraftLuaScripting plugin, int taskId) {
        TASKS_BY_PLUGIN.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(taskId);
    }

    private static void untrack(MinecraftLuaScripting plugin, int taskId) {
        Set<Integer> ids = TASKS_BY_PLUGIN.get(plugin.getName());
        if (ids == null) {
            return;
        }
        ids.remove(taskId);
        if (ids.isEmpty()) {
            TASKS_BY_PLUGIN.remove(plugin.getName());
        }
    }

    public static final class RunLater extends TwoArgFunction {
        private final MinecraftLuaScripting plugin;

        public RunLater(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public LuaValue call(LuaValue ticksVal, LuaValue callbackVal) {
            long ticks = ticksVal.checklong();
            if (!callbackVal.isfunction()) {
                return LuaValue.error("runLater(ticks, fn): fn must be a function");
            }

            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    callbackVal.call();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in scheduled Lua task: " + e);
                }
            }, ticks);

            track(plugin, task.getTaskId());
            return LuaValue.valueOf(task.getTaskId());
        }
    }

    public static final class RunRepeating extends VarArgFunction {
        private final MinecraftLuaScripting plugin;

        public RunRepeating(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public Varargs invoke(Varargs args) {
            long delay = args.arg(1).checklong();
            long period = args.arg(2).checklong();
            LuaValue callbackVal = args.arg(3);
            if (!callbackVal.isfunction()) {
                return LuaValue.error("runRepeating(delayTicks, periodTicks, fn): fn must be a function");
            }

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                try {
                    callbackVal.call();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in repeating Lua task: " + e);
                }
            }, delay, period);

            track(plugin, task.getTaskId());
            return LuaValue.valueOf(task.getTaskId());
        }
    }

    public static final class RunAsync extends OneArgFunction {
        private final MinecraftLuaScripting plugin;

        public RunAsync(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public LuaValue call(LuaValue callbackVal) {
            if (!callbackVal.isfunction()) {
                return LuaValue.error("runAsync(fn): fn must be a function");
            }

            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    callbackVal.call();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in async Lua task: " + e);
                }
            });

            track(plugin, task.getTaskId());
            return LuaValue.valueOf(task.getTaskId());
        }
    }

    public static final class RunAsyncLater extends TwoArgFunction {
        private final MinecraftLuaScripting plugin;

        public RunAsyncLater(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public LuaValue call(LuaValue ticksVal, LuaValue callbackVal) {
            long ticks = ticksVal.checklong();
            if (!callbackVal.isfunction()) {
                return LuaValue.error("runAsyncLater(ticks, fn): fn must be a function");
            }

            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                try {
                    callbackVal.call();
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in async delayed Lua task: " + e);
                }
            }, ticks);

            track(plugin, task.getTaskId());
            return LuaValue.valueOf(task.getTaskId());
        }
    }

    public static final class CancelTask extends OneArgFunction {
        private final MinecraftLuaScripting plugin;

        public CancelTask(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public LuaValue call(LuaValue taskIdVal) {
            int taskId = taskIdVal.checkint();
            Bukkit.getScheduler().cancelTask(taskId);
            untrack(plugin, taskId);
            return LuaValue.TRUE;
        }
    }

    public static final class CancelAllTasks extends OneArgFunction {
        private final MinecraftLuaScripting plugin;

        public CancelAllTasks(MinecraftLuaScripting plugin) {
            this.plugin = plugin;
        }

        @Override
        public LuaValue call(LuaValue ignored) {
            cancelAllTasks(plugin);
            return LuaValue.TRUE;
        }
    }
}