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

public final class s3$create_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3-api", (String)"create-bucket");

    public static Object invokeStatic(Object s32, Object bucket, Object region) {
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = region;
        region = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$create_bucket.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object s32, Object bucket) {
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$create_bucket.invokeStatic(object3, object4);
    }
}

