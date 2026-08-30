/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.RT;
import java.util.Map;

public interface Log {
    public static final Object T = RT.keyword(null, (String)"t");
    public static final Object DATA = RT.keyword(null, (String)"data");

    public Iterable<Map> txRange(Object var1, Object var2);
}

