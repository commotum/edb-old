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
import datomic.s3.Name;

public final class s3$delete_object
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object key) {
        v0 = (IFn)s3$delete_object.const__0.getRawRoot();
        v1 = s3;
        s3 = null;
        v2 = bucket;
        bucket = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == s3$delete_object.__cached_class__0) ** GOTO lbl11
        if (!(v3 instanceof Name)) {
            v3 = v3;
            s3$delete_object.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = s3$delete_object.const__1.getRawRoot().invoke(v3);
        } else {
            v4 = ((Name)v3).s3_name();
        }
        v5 = key;
        key = null;
        v6 = v5;
        if (Util.classOf((Object)v5) == s3$delete_object.__cached_class__1) ** GOTO lbl21
        if (!(v6 instanceof Name)) {
            v6 = v6;
            s3$delete_object.__cached_class__1 = Util.classOf((Object)v6);
lbl21:
            // 2 sources

            v7 = s3$delete_object.const__1.getRawRoot().invoke(v6);
        } else {
            v7 = ((Name)v6).s3_name();
        }
        return v0.invoke(v1, v4, v7);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$delete_object.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"delete-object");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
    }
}

