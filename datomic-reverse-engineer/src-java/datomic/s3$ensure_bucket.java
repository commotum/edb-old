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

public final class s3$ensure_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"get-bucket");
    public static final Var const__1 = RT.var((String)"datomic.s3", (String)"create-bucket");

    public static Object invokeStatic(Object s32, Object bucket, Object region) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(s32, bucket);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            Object object3 = s32;
            s32 = null;
            Object object4 = bucket;
            bucket = null;
            Object object5 = region;
            region = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4, object5);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$ensure_bucket.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object s32, Object bucket) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(s32, bucket);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            Object object3 = s32;
            s32 = null;
            Object object4 = bucket;
            bucket = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$ensure_bucket.invokeStatic(object3, object4);
    }
}

