/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class thread$fixed_thread_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"fixed-thread-pool");
    public static final Var const__1 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");

    public static Object invokeStatic(Object name_prefix, Object n, Object group) {
        Object object = n;
        n = null;
        Object object2 = name_prefix;
        name_prefix = null;
        Object object3 = group;
        group = null;
        return Executors.newFixedThreadPool(RT.intCast((Object)((Number)object)), (ThreadFactory)((IFn)const__1.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return thread$fixed_thread_pool.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object name_prefix, Object n) {
        Object object = name_prefix;
        name_prefix = null;
        Object object2 = n;
        n = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$fixed_thread_pool.invokeStatic(object3, object4);
    }
}

