/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.AmazonS3Client
 *  com.amazonaws.services.s3.model.S3Object
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import datomic.datafy.ObjectToData;

public final class s3_api$get_object
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1, Object x2) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        Object object4 = x2;
        x2 = null;
        S3Object s3Object = ((AmazonS3Client)object2).getObject((String)((IFn)const__1.getRawRoot()).invoke(object3, const__2), (String)((IFn)const__1.getRawRoot()).invoke(object4, const__2));
        if (Util.classOf((Object)s3Object) != __cached_class__0) {
            if (s3Object instanceof ObjectToData) {
                object = ((ObjectToData)s3Object).object_to_data();
                return object;
            }
            s3Object = s3Object;
            __cached_class__0 = Util.classOf((Object)s3Object);
        }
        object = const__0.getRawRoot().invoke((Object)s3Object);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3_api$get_object.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"java.lang.String");
    }
}

