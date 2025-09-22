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
import datomic.io.Coercions;

public final class backup$create_storage
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object storage_uri, Object sse_QMARK_) {
        v0 = (IFn)backup$create_storage.const__1.getRawRoot();
        v1 = storage_uri;
        storage_uri = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == backup$create_storage.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof Coercions)) {
            v2 = v2;
            backup$create_storage.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = backup$create_storage.const__2.getRawRoot().invoke(v2);
        } else {
            v3 = ((Coercions)v2).as_uri();
        }
        v4 = sse_QMARK_;
        sse_QMARK_ = null;
        return v0.invoke(v3, v4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$create_storage.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object storage_uri) {
        Object object = storage_uri;
        storage_uri = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$create_storage.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"create-storage");
        const__1 = RT.var((String)"datomic.backup", (String)"create-storage*");
        const__2 = RT.var((String)"datomic.io", (String)"as-uri");
    }
}

