/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$touch_heartbeat
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__1 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__5 = RT.var((String)"datomic.cluster", (String)"touch-ref");
    public static final Var const__6 = RT.var((String)"datomic.coordination", (String)"pod-key");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object cluster2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
        long n = 0L;
        while (true) {
            if (10L < n) {
                throw (Throwable)new RuntimeException("Retry limit exceeded claiming transator heartbeat.");
            }
            Object object2 = ((IFn)const__5.getRawRoot()).invoke(cluster2, const__6.getRawRoot());
            if (object2 != null && object2 != Boolean.FALSE) break;
            n = Numbers.inc((long)n);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$touch_heartbeat.invokeStatic(object2);
    }
}

