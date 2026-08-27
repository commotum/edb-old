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
import datomic.peer.RemoteConnection;

public final class tools$get_val
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object conn, Object k) {
        v0 = conn;
        conn = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == tools$get_val.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof RemoteConnection)) {
            v1 = v1;
            tools$get_val.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = tools$get_val.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((RemoteConnection)v1).get_olookup();
        }
        v3 = k;
        k = null;
        return RT.get((Object)v2, (Object)v3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$get_val.invokeStatic(object3, object4);
    }

    static {
        const__1 = RT.var((String)"datomic.peer", (String)"get-olookup");
    }
}

