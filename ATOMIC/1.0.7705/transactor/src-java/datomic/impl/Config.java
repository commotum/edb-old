package datomic.impl;

import clojure.lang.RT;

/**
 * Holds process-wide serialization extension handlers and enables the JRuby
 * integration layer. Read and write handlers are published through volatile
 * fields so all threads in the process observe configuration changes.
 */
public class Config {
    private static volatile Object readHandlers;
    private static volatile Object writeHandlers;

    public static Object getWriteHandlers() {
        return writeHandlers;
    }

    public static Object getReadHandlers() {
        return readHandlers;
    }

    public static void setReadHandlers(Object handlers) {
        readHandlers = handlers;
    }

    public static void setWriteHandlers(Object handlers) {
        writeHandlers = handlers;
    }

    public static void jrubyMode(Object ruby, Object comparators) throws Exception {
        RT.load((String)"datomic/jruby");
        RT.var((String)"datomic.jruby", (String)"jruby-mode").invoke(ruby);
    }
}
