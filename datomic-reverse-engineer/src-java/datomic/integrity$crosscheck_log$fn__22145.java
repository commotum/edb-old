/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;

public final class integrity$crosscheck_log$fn__22145
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__0, null});
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

