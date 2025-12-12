/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.AmazonServiceException
 *  com.amazonaws.services.s3.model.AmazonS3Exception
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import datomic.s3.Name;

public final class s3$object_exists_QMARK_
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final AFn const__3;
    public static final AFn const__6;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object k) {
        try {
            v0 = (IFn)s3$object_exists_QMARK_.const__0.getRawRoot();
            v1 = s3;
            s3 = null;
            v2 = bucket;
            bucket = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == s3$object_exists_QMARK_.__cached_class__0) ** GOTO lbl12
            if (!(v3 instanceof Name)) {
                v3 = v3;
                s3$object_exists_QMARK_.__cached_class__0 = Util.classOf((Object)v3);
lbl12:
                // 2 sources

                v4 = s3$object_exists_QMARK_.const__1.getRawRoot().invoke(v3);
            } else {
                v4 = ((Name)v3).s3_name();
            }
            v5 = k;
            k = null;
            v0.invoke(v1, v4, v5);
            var3_3 = s3$object_exists_QMARK_.const__3;
        }
        catch (AmazonS3Exception se) {
            if (404L != (long)((AmazonServiceException)se).getStatusCode()) {
                se = null;
                throw (Throwable)se;
            }
            var3_3 = s3$object_exists_QMARK_.const__6;
        }
        return var3_3;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$object_exists_QMARK_.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"get-object-metadata");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"exists"), Boolean.TRUE});
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"exists"), Boolean.FALSE});
    }
}

