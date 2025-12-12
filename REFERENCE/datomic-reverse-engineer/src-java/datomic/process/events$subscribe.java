/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.process;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class events$subscribe
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"datomic.process.events", (String)"subscribers-ref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");

    public static Object invokeStatic(Object reference, Object key, Object fn2) {
        Object object = key;
        key = null;
        Object object2 = reference;
        reference = null;
        Object object3 = fn2;
        fn2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), const__2.getRawRoot(), object, const__3.getRawRoot(), object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return events$subscribe.invokeStatic(object4, object5, object6);
    }
}

