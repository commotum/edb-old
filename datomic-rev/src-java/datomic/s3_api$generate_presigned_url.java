/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.HttpMethod
 *  com.amazonaws.services.s3.AmazonS3Client
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import datomic.datafy.ObjectToData;
import java.net.URL;
import java.util.Date;

public final class s3_api$generate_presigned_url
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;
    public static final Object const__3;
    public static final Object const__4;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1, Object x2, Object x3, Object x4) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        Object object4 = x2;
        x2 = null;
        Object object5 = x3;
        x3 = null;
        Object object6 = x4;
        x4 = null;
        URL uRL = ((AmazonS3Client)object2).generatePresignedUrl((String)((IFn)const__1.getRawRoot()).invoke(object3, const__2), (String)((IFn)const__1.getRawRoot()).invoke(object4, const__2), (Date)((IFn)const__1.getRawRoot()).invoke(object5, const__3), (HttpMethod)((IFn)const__1.getRawRoot()).invoke(object6, const__4));
        if (Util.classOf((Object)uRL) != __cached_class__0) {
            if (uRL instanceof ObjectToData) {
                object = ((ObjectToData)((Object)uRL)).object_to_data();
                return object;
            }
            uRL = uRL;
            __cached_class__0 = Util.classOf((Object)uRL);
        }
        object = const__0.getRawRoot().invoke((Object)uRL);
        return object;
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
        return s3_api$generate_presigned_url.invokeStatic(object6, object7, object8, object9, object10);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"java.lang.String");
        const__3 = RT.classForName((String)"java.util.Date");
        const__4 = RT.classForName((String)"com.amazonaws.HttpMethod");
    }
}

