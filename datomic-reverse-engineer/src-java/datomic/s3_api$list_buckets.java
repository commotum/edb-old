/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.AmazonS3Client
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.AmazonS3Client;
import datomic.datafy.ObjectToData;
import java.util.List;

public final class s3_api$list_buckets
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o) {
        Object object;
        Object object2 = o;
        o = null;
        List list = ((AmazonS3Client)object2).listBuckets();
        if (Util.classOf((Object)list) != __cached_class__0) {
            if (list instanceof ObjectToData) {
                object = ((ObjectToData)((Object)list)).object_to_data();
                return object;
            }
            list = list;
            __cached_class__0 = Util.classOf((Object)list);
        }
        object = const__0.getRawRoot().invoke((Object)list);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$list_buckets.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
    }
}

