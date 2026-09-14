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

public final class s3$put_object
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object path, Object stream, Object metadata) {
        v0 = (IFn)s3$put_object.const__0.getRawRoot();
        v1 = s3;
        s3 = null;
        v2 = bucket;
        bucket = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == s3$put_object.__cached_class__0) ** GOTO lbl11
        if (!(v3 instanceof Name)) {
            v3 = v3;
            s3$put_object.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = s3$put_object.const__1.getRawRoot().invoke(v3);
        } else {
            v4 = ((Name)v3).s3_name();
        }
        v5 = path;
        path = null;
        v6 = stream;
        stream = null;
        v7 = metadata;
        metadata = null;
        return v0.invoke(v1, v4, v5, v6, v7);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return s3$put_object.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"put-object");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
    }
}

