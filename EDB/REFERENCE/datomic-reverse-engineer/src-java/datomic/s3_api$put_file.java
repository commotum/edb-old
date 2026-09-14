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
 *  com.amazonaws.services.s3.model.PutObjectResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import datomic.datafy.ObjectToData;
import java.io.File;

public final class s3_api$put_file
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;
    public static final Object const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1, Object x2, Object x3) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        Object object4 = x2;
        x2 = null;
        Object object5 = x3;
        x3 = null;
        PutObjectResult putObjectResult = ((AmazonS3Client)object2).putObject((String)((IFn)const__1.getRawRoot()).invoke(object3, const__2), (String)((IFn)const__1.getRawRoot()).invoke(object4, const__2), (File)((IFn)const__1.getRawRoot()).invoke(object5, const__3));
        if (Util.classOf((Object)putObjectResult) != __cached_class__0) {
            if (putObjectResult instanceof ObjectToData) {
                object = ((ObjectToData)putObjectResult).object_to_data();
                return object;
            }
            putObjectResult = putObjectResult;
            __cached_class__0 = Util.classOf((Object)putObjectResult);
        }
        object = const__0.getRawRoot().invoke((Object)putObjectResult);
        return object;
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
        return s3_api$put_file.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"java.lang.String");
        const__3 = RT.classForName((String)"java.io.File");
    }
}

