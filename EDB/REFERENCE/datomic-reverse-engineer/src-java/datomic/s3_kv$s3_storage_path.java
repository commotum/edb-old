/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class s3_kv$s3_storage_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object base, Object k) {
        Object object = base;
        base = null;
        String string = ((String)k).contains("/") ? null : "data/";
        Object object2 = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)"/", (Object)string, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3_kv$s3_storage_path.invokeStatic(object3, object4);
    }
}

