package me.touchie771.minecraftLuaScripting.api;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import java.util.logging.Logger;

public class LoggerApi {

    public static class Info extends OneArgFunction {
        private final Logger logger;

        public Info(Logger logger) {
            this.logger = logger;
        }

        @Override
        public LuaValue call(LuaValue arg) {
            logger.info(arg.checkjstring());
            return LuaValue.NONE;
        }
    }

    public static class Warning extends OneArgFunction {
        private final Logger logger;

        public Warning(Logger logger) {
            this.logger = logger;
        }

        @Override
        public LuaValue call(LuaValue arg) {
            logger.warning(arg.checkjstring());
            return LuaValue.NONE;
        }
    }

    public static class Error extends OneArgFunction {
        private final Logger logger;

        public Error(Logger logger) {
            this.logger = logger;
        }

        @Override
        public LuaValue call(LuaValue arg) {
            logger.severe(arg.checkjstring());
            return LuaValue.NONE;
        }
    }
}