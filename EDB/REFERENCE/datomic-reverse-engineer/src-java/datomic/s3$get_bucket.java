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
import datomic.s3$get_bucket$fn__23286;

public final class s3$get_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"datomic.s3-api", (String)"list-buckets");

    public static Object invokeStatic(Object s32, Object bucket_name) {
        Object object = bucket_name;
        bucket_name = null;
        Object object2 = s32;
        s32 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new s3$get_bucket$fn__23286(object), ((IFn)const__2.getRawRoot()).invoke(object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$get_bucket.invokeStatic(object3, object4);
    }
}

