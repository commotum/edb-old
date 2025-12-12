/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.s3.Name;

public final class s3$list_objects
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object prefix, Object delimiter) {
        v0 = (IFn)s3$list_objects.const__2.getRawRoot();
        v1 = s3$list_objects.const__3.getRawRoot();
        v2 = (IFn)s3$list_objects.const__4.getRawRoot();
        v3 = s3;
        v4 = (IFn)s3$list_objects.const__5.getRawRoot();
        v5 = s3;
        s3 = null;
        v6 = new Object[6];
        v6[0] = s3$list_objects.const__6;
        v7 = prefix;
        prefix = null;
        v6[1] = v7;
        v6[2] = s3$list_objects.const__7;
        v8 = delimiter;
        delimiter = null;
        v6[3] = v8;
        v6[4] = s3$list_objects.const__8;
        v9 = bucket;
        bucket = null;
        v10 = v9;
        if (Util.classOf((Object)v9) == s3$list_objects.__cached_class__1) ** GOTO lbl25
        if (!(v10 instanceof Name)) {
            v10 = v10;
            s3$list_objects.__cached_class__1 = Util.classOf((Object)v10);
lbl25:
            // 2 sources

            v11 = s3$list_objects.const__1.getRawRoot().invoke(v10);
        } else {
            v11 = ((Name)v10).s3_name();
        }
        v6[5] = v11;
        return v0.invoke(v1, v2.invoke(v3, v4.invoke(v5, (Object)RT.mapUniqueKeys((Object[])v6))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return s3$list_objects.invokeStatic(object5, object6, object7, object8);
    }

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket) {
        v0 = (IFn)s3$list_objects.const__0.getRawRoot();
        v1 = bucket;
        bucket = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == s3$list_objects.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof Name)) {
            v2 = v2;
            s3$list_objects.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = s3$list_objects.const__1.getRawRoot().invoke(v2);
        } else {
            v3 = ((Name)v2).s3_name();
        }
        result = v0.invoke(s3, v3);
        v4 = s3;
        s3 = null;
        v5 = result;
        result = null;
        return ((IFn)s3$list_objects.const__2.getRawRoot()).invoke(s3$list_objects.const__3.getRawRoot(), ((IFn)s3$list_objects.const__4.getRawRoot()).invoke(v4, v5));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$list_objects.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"list-objects");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__2 = RT.var((String)"clojure.core", (String)"apply");
        const__3 = RT.var((String)"clojure.core", (String)"concat");
        const__4 = RT.var((String)"datomic.s3", (String)"-list-objects-seq");
        const__5 = RT.var((String)"datomic.s3-api", (String)"list-objects-from-request");
        const__6 = RT.keyword(null, (String)"prefix");
        const__7 = RT.keyword(null, (String)"delimiter");
        const__8 = RT.keyword(null, (String)"bucketName");
    }
}

