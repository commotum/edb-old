/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.S3Object
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.S3Object;
import datomic.s3.Name;

public final class s3$get_bytes
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object path) {
        v0 = (IFn)s3$get_bytes.const__0.getRawRoot();
        v1 = s3;
        s3 = null;
        v2 = bucket;
        bucket = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == s3$get_bytes.__cached_class__0) ** GOTO lbl11
        if (!(v3 instanceof Name)) {
            v3 = v3;
            s3$get_bytes.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = s3$get_bytes.const__1.getRawRoot().invoke(v3);
        } else {
            v4 = ((Name)v3).s3_name();
        }
        v5 = path;
        path = null;
        v6 = v5;
        if (Util.classOf((Object)v5) == s3$get_bytes.__cached_class__1) ** GOTO lbl21
        if (!(v6 instanceof Name)) {
            v6 = v6;
            s3$get_bytes.__cached_class__1 = Util.classOf((Object)v6);
lbl21:
            // 2 sources

            v7 = s3$get_bytes.const__1.getRawRoot().invoke(v6);
        } else {
            v7 = ((Name)v6).s3_name();
        }
        v8 = temp__5457__auto__23300 = v0.invoke(v1, v4, v7);
        if (v8 != null && v8 != Boolean.FALSE) {
            v9 = temp__5457__auto__23300;
            temp__5457__auto__23300 = null;
            obj = v9;
            is = ((S3Object)obj).getObjectContent();
            try {
                v10 = obj;
                obj = null;
                ba = Numbers.byte_array((Object)Numbers.num((long)((S3Object)v10).getObjectMetadata().getContentLength()));
                ((IFn)s3$get_bytes.const__3.getRawRoot()).invoke((Object)is, (Object)ba);
                v11 = ba;
                ba = null;
                var7_7 = v11;
            }
            finally {
                v12 = is;
                is = null;
                v12.close();
            }
            v13 = var7_7;
        } else {
            v13 = null;
        }
        return v13;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$get_bytes.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.s3", (String)"get-object");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__3 = RT.var((String)"datomic.io", (String)"fill-array");
    }
}

