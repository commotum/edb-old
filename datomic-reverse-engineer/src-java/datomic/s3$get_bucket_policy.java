/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.s3.Name;

public final class s3$get_bucket_policy
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object s32, Object bucket) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = s32;
        s32 = null;
        Object object3 = bucket;
        bucket = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof Name) {
                object = ((Name)object4).s3_name();
                return iFn.invoke(object2, object);
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        object = const__1.getRawRoot().invoke(object4);
        return iFn.invoke(object2, object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$get_bucket_policy.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.s3-api", (String)"get-bucket-policy");
        const__1 = RT.var((String)"datomic.s3", (String)"s3-name");
    }
}

