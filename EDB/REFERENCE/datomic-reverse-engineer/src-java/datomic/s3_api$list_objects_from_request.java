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
 *  com.amazonaws.services.s3.model.ListObjectsRequest
 *  com.amazonaws.services.s3.model.ObjectListing
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import datomic.datafy.ObjectToData;

public final class s3_api$list_objects_from_request
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        ObjectListing objectListing = ((AmazonS3Client)object2).listObjects((ListObjectsRequest)((IFn)const__1.getRawRoot()).invoke(object3, const__2));
        if (Util.classOf((Object)objectListing) != __cached_class__0) {
            if (objectListing instanceof ObjectToData) {
                object = ((ObjectToData)objectListing).object_to_data();
                return object;
            }
            objectListing = objectListing;
            __cached_class__0 = Util.classOf((Object)objectListing);
        }
        object = const__0.getRawRoot().invoke((Object)objectListing);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3_api$list_objects_from_request.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"com.amazonaws.services.s3.model.ListObjectsRequest");
    }
}

