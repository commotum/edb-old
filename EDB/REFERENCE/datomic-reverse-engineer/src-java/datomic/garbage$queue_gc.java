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
import datomic.garbage$queue_gc$fn__19869;

public final class garbage$queue_gc
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"send-off");
    public static final Var const__1 = RT.var((String)"datomic.garbage", (String)"collection-agent");

    public static Object invokeStatic(Object cluster2, Object older_than) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = older_than;
        older_than = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)new garbage$queue_gc$fn__19869(object, object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$queue_gc.invokeStatic(object3, object4);
    }
}

