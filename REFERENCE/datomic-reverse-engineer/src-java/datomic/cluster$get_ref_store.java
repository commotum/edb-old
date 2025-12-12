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
import datomic.cluster.RefClusterStore;

public final class cluster$get_ref_store
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cs) {
        Object object;
        Object object2 = cs;
        cs = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof RefClusterStore) {
                object = ((RefClusterStore)object3)._get_ref_store();
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__0.getRawRoot().invoke(object3);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$get_ref_store.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"-get-ref-store");
    }
}

