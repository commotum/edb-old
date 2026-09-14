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
import datomic.async$daemon$fn__9434;

public final class async$daemon
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__2 = RT.var((String)"datomic.async", (String)"name-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"inc");
    public static final Object const__6 = 0L;
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"datomic.async", (String)"binding-conveyor-fn");

    public static Object invokeStatic(Object f, Object base) {
        Object bound_f;
        Object n = RT.get((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), const__3.getRawRoot(), base, ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), const__6)), (Object)base);
        Object object = base;
        base = null;
        Object object2 = n;
        n = null;
        Object name = ((IFn)const__7.getRawRoot()).invoke(object, (Object)"-", object2);
        Object object3 = f;
        f = null;
        Object object4 = bound_f = ((IFn)const__8.getRawRoot()).invoke(object3);
        bound_f = null;
        Thread G__9433 = new Thread((Runnable)((Object)new async$daemon$fn__9434(object4, name)));
        Object object5 = name;
        name = null;
        G__9433.setName((String)object5);
        G__9433.setDaemon(Boolean.TRUE);
        G__9433.start();
        Thread thread2 = G__9433;
        G__9433 = null;
        return thread2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return async$daemon.invokeStatic(object3, object4);
    }
}

