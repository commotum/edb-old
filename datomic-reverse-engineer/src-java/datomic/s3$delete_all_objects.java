/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class s3$delete_all_objects
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"delete-objects");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__2 = RT.keyword(null, (String)"key");
    public static final Var const__3 = RT.var((String)"datomic.s3", (String)"list-objects");

    public static Object invokeStatic(Object s32, Object bucket) {
        Object object = s32;
        Object object2 = bucket;
        Object object3 = s32;
        s32 = null;
        Object object4 = bucket;
        bucket = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke(object3, object4)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$delete_all_objects.invokeStatic(object3, object4);
    }
}

