/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.UUID;

public final class s3$new_bucket
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"create-bucket");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object s32, Object basename) {
        Object object = s32;
        s32 = null;
        Object object2 = basename;
        basename = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2, (Object)"-", (Object)UUID.randomUUID()));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$new_bucket.invokeStatic(object3, object4);
    }
}

