/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class process_monitor$metrics_callback$fn__23501
extends AFunction {
    Object s;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__2 = RT.var((String)"datomic.callback", (String)"create-callback");
    public static final Keyword const__3 = RT.keyword(null, (String)"threw");

    public process_monitor$metrics_callback$fn__23501(Object object) {
        this.s = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object G__23502 = this.s = null;
            if (Util.identical((Object)G__23502, null)) {
                object = null;
            } else {
                Object object2 = G__23502;
                G__23502 = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object2);
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

