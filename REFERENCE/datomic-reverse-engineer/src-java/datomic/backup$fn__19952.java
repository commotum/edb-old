/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.MethodImplCache
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.MethodImplCache;
import datomic.backup$fn__19952$G__19945__19959;
import datomic.backup$fn__19952$G__19946__19955;

public final class backup$fn__19952
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        backup$fn__19952$G__19946__19955 G__19946;
        backup$fn__19952$G__19946__19955 backup$fn__19952$G__19946__19955 = G__19946 = new backup$fn__19952$G__19946__19955();
        G__19946 = null;
        backup$fn__19952$G__19945__19959 f__7646__auto__19964 = new backup$fn__19952$G__19945__19959((Object)backup$fn__19952$G__19946__19955);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__19964).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__19964;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__19952.invokeStatic(object2);
    }
}

