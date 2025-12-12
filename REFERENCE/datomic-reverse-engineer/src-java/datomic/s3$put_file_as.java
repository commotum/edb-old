/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.CannedAccessControlList
 *  com.amazonaws.services.s3.model.ObjectMetadata
 *  com.amazonaws.services.s3.model.PutObjectRequest
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import datomic.s3.Name;
import java.io.InputStream;

public final class s3$put_file_as
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Object const__6;
    public static final Var const__7;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object f, Object path, Object metadata, Object canned_acl) {
        v0 = bucket;
        bucket = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == s3$put_file_as.__cached_class__2) ** GOTO lbl8
        if (!(v1 instanceof Name)) {
            v1 = v1;
            s3$put_file_as.__cached_class__2 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = s3$put_file_as.const__1.getRawRoot().invoke(v1);
        } else {
            v2 = ((Name)v1).s3_name();
        }
        v3 = path;
        path = null;
        v4 = f;
        f = null;
        v5 = metadata;
        metadata = null;
        v6 = canned_acl;
        canned_acl = null;
        req = new PutObjectRequest((String)v2, (String)v3, (InputStream)((IFn)s3$put_file_as.const__4.getRawRoot()).invoke(((IFn)s3$put_file_as.const__2.getRawRoot()).invoke(v4)), (ObjectMetadata)((IFn)s3$put_file_as.const__5.getRawRoot()).invoke(v5, s3$put_file_as.const__6)).withCannedAcl((CannedAccessControlList)v6);
        v7 = s3;
        s3 = null;
        v8 = req;
        req = null;
        return ((IFn)s3$put_file_as.const__7.getRawRoot()).invoke(v7, (Object)v8);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return s3$put_file_as.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object f, Object path, Object metadata) {
        v0 = (IFn)s3$put_file_as.const__3.getRawRoot();
        v1 = s3;
        s3 = null;
        v2 = bucket;
        bucket = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == s3$put_file_as.__cached_class__1) ** GOTO lbl11
        if (!(v3 instanceof Name)) {
            v3 = v3;
            s3$put_file_as.__cached_class__1 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = s3$put_file_as.const__1.getRawRoot().invoke(v3);
        } else {
            v4 = ((Name)v3).s3_name();
        }
        v5 = path;
        path = null;
        v6 = f;
        f = null;
        v7 = metadata;
        metadata = null;
        return v0.invoke(v1, v4, v5, ((IFn)s3$put_file_as.const__4.getRawRoot()).invoke(((IFn)s3$put_file_as.const__2.getRawRoot()).invoke(v6)), v7);
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
        return s3$put_file_as.invokeStatic(object6, object7, object8, object9, object10);
    }

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object s3, Object bucket, Object f, Object path) {
        v0 = (IFn)s3$put_file_as.const__0.getRawRoot();
        v1 = s3;
        s3 = null;
        v2 = bucket;
        bucket = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == s3$put_file_as.__cached_class__0) ** GOTO lbl11
        if (!(v3 instanceof Name)) {
            v3 = v3;
            s3$put_file_as.__cached_class__0 = Util.classOf((Object)v3);
lbl11:
            // 2 sources

            v4 = s3$put_file_as.const__1.getRawRoot().invoke(v3);
        } else {
            v4 = ((Name)v3).s3_name();
        }
        v5 = path;
        path = null;
        v6 = f;
        f = null;
        return v0.invoke(v1, v4, v5, ((IFn)s3$put_file_as.const__2.getRawRoot()).invoke(v6));
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
        return s3$put_file_as.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"put-file");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__2 = RT.var((String)"clojure.java.io", (String)"file");
        const__3 = RT.var((String)"datomic.s3-api", (String)"put-object");
        const__4 = RT.var((String)"clojure.java.io", (String)"input-stream");
        const__5 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__6 = RT.classForName((String)"com.amazonaws.services.s3.model.ObjectMetadata");
        const__7 = RT.var((String)"datomic.s3-api", (String)"put-object-with-canned-acl");
    }
}

