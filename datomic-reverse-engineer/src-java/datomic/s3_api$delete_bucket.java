/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.AmazonS3Client
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.s3.AmazonS3Client;

public final class s3_api$delete_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__1 = RT.classForName((String)"java.lang.String");
    public static final Keyword const__2 = RT.keyword(null, (String)"ok");

    public static Object invokeStatic(Object o, Object x1) {
        Object object = o;
        o = null;
        Object object2 = x1;
        x1 = null;
        ((AmazonS3Client)object).deleteBucket((String)((IFn)const__0.getRawRoot()).invoke(object2, const__1));
        return const__2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3_api$delete_bucket.invokeStatic(object3, object4);
    }
}

