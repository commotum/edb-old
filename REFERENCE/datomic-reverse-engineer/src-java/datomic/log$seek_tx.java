/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.log.LogSeek;

public final class log$seek_tx
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object provider2, Object t) {
        Object object;
        Object object2 = provider2;
        provider2 = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof LogSeek) {
                Object object4 = t;
                t = null;
                object = ((LogSeek)object3).seek_tx_impl(object4);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = t;
        t = null;
        object = const__0.getRawRoot().invoke(object3, object5);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$seek_tx.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"seek-tx-impl");
    }
}

