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
import datomic.peer$shutdown$fn__21626;

public final class peer$shutdown
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"connection-lock");
    public static final Var const__1 = RT.var((String)"datomic.connector", (String)"stop-all-connectors");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__3 = RT.var((String)"datomic.cluster-stack", (String)"kv-cache-ref");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"shutdown-agents");

    public static Object invokeStatic(Object shutdown_clojure) {
        Object lockee__5436__auto__21637;
        Object object = lockee__5436__auto__21637 = const__0.getRawRoot();
        lockee__5436__auto__21637 = null;
        ((IFn)new peer$shutdown$fn__21626(object)).invoke();
        ((IFn)const__1.getRawRoot()).invoke();
        ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), null);
        Object object2 = shutdown_clojure;
        shutdown_clojure = null;
        return object2 != null && object2 != Boolean.FALSE ? ((IFn)const__4.getRawRoot()).invoke() : null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$shutdown.invokeStatic(object2);
    }
}

