/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic.impl;

import clojure.lang.RT;

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

