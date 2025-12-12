/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.integrity$crosscheck_indexes$fn__22207$fn__22212;

public final class integrity$crosscheck_indexes$fn__22207
extends AFunction {
    Object o;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public integrity$crosscheck_indexes$fn__22207(Object object) {
        this.o = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.o = null;
            objectArray[1] = ((IFn)new integrity$crosscheck_indexes$fn__22207$fn__22212(this.o)).invoke();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
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

