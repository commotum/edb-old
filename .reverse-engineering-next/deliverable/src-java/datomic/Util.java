/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Util {
    private static final Var REQUIRE = RT.var((String)"clojure.core", (String)"require");
    private static final Var NAME;
    private static final Var NAMESPACE;
    private static final Var READ_STRING;
    private static final Var READ_ALL;

    private Util() {
    }

    public static String name(Object k) {
        return (String)NAME.invoke(k);
    }

    public static String namespace(Object k) {
        return (String)NAMESPACE.invoke(k);
    }

    public static List list(Object ... items) {
        if (items == null) {
            return new ArrayList();
        }
        ArrayList<Object> list = new ArrayList<Object>(items.length);
        for (int i = 0; i < items.length; ++i) {
            list.add(items[i]);
        }
        return Collections.unmodifiableList(list);
    }

    public static Map map(Object ... keyvals) {
        if (keyvals == null) {
            return new HashMap();
        }
        if (keyvals.length % 2 != 0) {
            throw new IllegalArgumentException("Map must have an even number of elements");
        }
        HashMap<Object, Object> m = new HashMap<Object, Object>(keyvals.length / 2);
        for (int i = 0; i < keyvals.length; i += 2) {
            m.put(keyvals[i], keyvals[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }

    public static Object read(String source) {
        return READ_STRING.invoke((Object)source);
    }

    public static List readAll(Reader reader2) {
        return (List)READ_ALL.invoke((Object)reader2);
    }

    public static Stream streamOn(Iterable it) {
        if (it == null) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it.iterator(), 1040), false);
    }

    static {
        REQUIRE.invoke((Object)Symbol.intern((String)"clojure.edn"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.io"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.db"));
        NAME = RT.var((String)"clojure.core", (String)"name");
        NAMESPACE = RT.var((String)"clojure.core", (String)"namespace");
        READ_STRING = RT.var((String)"clojure.edn", (String)"read-string");
        READ_ALL = RT.var((String)"datomic.io", (String)"read-all");
    }
}

