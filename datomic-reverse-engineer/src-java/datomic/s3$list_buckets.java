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

public final class s3$list_buckets
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3-api", (String)"list-buckets");

    public static Object invokeStatic(Object s32) {
        Object object = s32;
        s32 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3$list_buckets.invokeStatic(object2);
    }
}

