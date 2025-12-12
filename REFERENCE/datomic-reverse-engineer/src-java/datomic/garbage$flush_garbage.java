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

public final class garbage$flush_garbage
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"send-off");
    public static final Var const__1 = RT.var((String)"datomic.garbage", (String)"garbage-agent");
    public static final Var const__2 = RT.var((String)"datomic.garbage", (String)"do-mark-garbage");
    public static final Object const__3 = 0L;
    public static final Object const__4 = 800L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"await");

    public static Object invokeStatic(Object cluster2, Object olookup) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = olookup;
        olookup = null;
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), const__2.getRawRoot(), object, object2, null, const__3, const__4);
        return ((IFn)const__5.getRawRoot()).invoke(const__1.getRawRoot());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$flush_garbage.invokeStatic(object3, object4);
    }
}

