/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.java.io.Coercions
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.S3Object
 */
package datomic;

import clojure.java.io.Coercions;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.S3Object;
import datomic.s3.Name;

public final class s3$get_file
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object path, Object dest) {
        block11: {
            block10: {
                v0 = (IFn)s3$get_file.const__0.getRawRoot();
                v1 = s3;
                s3 = null;
                v2 = bucket;
                bucket = null;
                v3 = v2;
                if (Util.classOf((Object)v2) == s3$get_file.__cached_class__0) ** GOTO lbl11
                if (!(v3 instanceof Name)) {
                    v3 = v3;
                    s3$get_file.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
                    // 2 sources

                    v4 = s3$get_file.const__1.getRawRoot().invoke(v3);
                } else {
                    v4 = ((Name)v3).s3_name();
                }
                v5 = path;
                path = null;
                v6 = v5;
                if (Util.classOf((Object)v5) == s3$get_file.__cached_class__1) ** GOTO lbl21
                if (!(v6 instanceof Name)) {
                    v6 = v6;
                    s3$get_file.__cached_class__1 = Util.classOf((Object)v6);
lbl21:
                    // 2 sources

                    v7 = s3$get_file.const__1.getRawRoot().invoke(v6);
                } else {
                    v7 = ((Name)v6).s3_name();
                }
                v8 = temp__5457__auto__23308 = v0.invoke(v1, v4, v7);
                if (v8 == null || v8 == Boolean.FALSE) break block10;
                v9 = temp__5457__auto__23308;
                temp__5457__auto__23308 = null;
                v10 = obj = v9;
                obj = null;
                is = ((S3Object)v10).getObjectContent();
                try {
                    v11 = (IFn)s3$get_file.const__2.getRawRoot();
                    v12 = dest;
                    dest = null;
                    v13 = v12;
                    if (Util.classOf((Object)v12) == s3$get_file.__cached_class__2) ** GOTO lbl40
                    if (!(v13 instanceof Coercions)) {
                        v13 = v13;
                        s3$get_file.__cached_class__2 = Util.classOf((Object)v13);
lbl40:
                        // 2 sources

                        v14 = s3$get_file.const__3.getRawRoot().invoke(v13);
                    } else {
                        v14 = ((Coercions)v13).as_file();
                    }
                    var7_7 = v11.invoke((Object)is, v14);
                }
                finally {
                    v15 = is;
                    is = null;
                    v15.close();
                }
                v16 = var7_7;
                break block11;
            }
            v16 = null;
        }
        return v16;
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
        return s3$get_file.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.s3", (String)"get-object");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__2 = RT.var((String)"clojure.java.io", (String)"copy");
        const__3 = RT.var((String)"clojure.java.io", (String)"as-file");
    }
}

