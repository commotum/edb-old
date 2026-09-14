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
import datomic.backup$fn__19965$G__19943__19972;
import datomic.backup$fn__19965$G__19944__19968;

public final class backup$fn__19965
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        backup$fn__19965$G__19944__19968 G__19944;
        backup$fn__19965$G__19944__19968 backup$fn__19965$G__19944__19968 = G__19944 = new backup$fn__19965$G__19944__19968();
        G__19944 = null;
        backup$fn__19965$G__19943__19972 f__7646__auto__19977 = new backup$fn__19965$G__19943__19972((Object)backup$fn__19965$G__19944__19968);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__19977).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__19977;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__19965.invokeStatic(object2);
    }
}

