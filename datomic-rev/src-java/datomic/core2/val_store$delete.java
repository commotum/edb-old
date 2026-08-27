/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.spi.Delete;

public final class val_store$delete
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object val_store2, Object k, Object opts) {
        Object object;
        Object object2 = val_store2;
        val_store2 = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof Delete) {
                Object object4 = k;
                k = null;
                Object object5 = opts;
                opts = null;
                object = ((Delete)object3)._delete(object4, object5);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object6 = k;
        k = null;
        Object object7 = opts;
        opts = null;
        object = const__1.getRawRoot().invoke(object3, object6, object7);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return val_store$delete.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object val_store2, Object k) {
        Object object = val_store2;
        val_store2 = null;
        Object object2 = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return val_store$delete.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store", (String)"delete");
        const__1 = RT.var((String)"datomic.core2.val-store.spi", (String)"-delete");
    }
}

