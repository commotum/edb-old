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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class log$zip_and_create
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Object const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cs, Object uuid, Object buf) {
        Object object;
        Object object2 = cs;
        cs = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof ClusteredStore) {
                Object object4 = uuid;
                uuid = null;
                Object object5 = buf;
                buf = null;
                object = ((ClusteredStore)object3).create_val(const__1, ((IFn)const__2.getRawRoot()).invoke(object4), ((IFn)const__3.getRawRoot()).invoke(object5));
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object6 = uuid;
        uuid = null;
        Object object7 = buf;
        buf = null;
        object = const__0.getRawRoot().invoke(object3, const__1, ((IFn)const__2.getRawRoot()).invoke(object6), ((IFn)const__3.getRawRoot()).invoke(object7));
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$zip_and_create.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__1 = 2L;
        const__2 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__3 = RT.var((String)"datomic.io", (String)"gzip-buffer");
    }
}

