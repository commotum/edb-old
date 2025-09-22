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
import datomic.cluster$fn__10536$G__10440__10543;
import datomic.cluster$fn__10536$G__10441__10539;

public final class cluster$fn__10536
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        cluster$fn__10536$G__10441__10539 G__10441;
        cluster$fn__10536$G__10441__10539 cluster$fn__10536$G__10441__10539 = G__10441 = new cluster$fn__10536$G__10441__10539();
        G__10441 = null;
        cluster$fn__10536$G__10440__10543 f__7646__auto__10548 = new cluster$fn__10536$G__10440__10543((Object)cluster$fn__10536$G__10441__10539);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10548).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10548;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$fn__10536.invokeStatic(object2);
    }
}

