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

public final class s3$destroy_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"delete-all-objects");
    public static final Var const__1 = RT.var((String)"datomic.s3", (String)"delete-bucket");

    public static Object invokeStatic(Object s32, Object bucket) {
        ((IFn)const__0.getRawRoot()).invoke(s32, bucket);
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$destroy_bucket.invokeStatic(object3, object4);
    }
}

