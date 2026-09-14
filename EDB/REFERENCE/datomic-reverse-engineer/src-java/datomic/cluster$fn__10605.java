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
import datomic.cluster$fn__10605$G__10600__10614;
import datomic.cluster$fn__10605$G__10601__10609;

public final class cluster$fn__10605
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        cluster$fn__10605$G__10601__10609 G__10601;
        cluster$fn__10605$G__10601__10609 cluster$fn__10605$G__10601__10609 = G__10601 = new cluster$fn__10605$G__10601__10609();
        G__10601 = null;
        cluster$fn__10605$G__10600__10614 f__7646__auto__10619 = new cluster$fn__10605$G__10600__10614((Object)cluster$fn__10605$G__10601__10609);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10619).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10619;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$fn__10605.invokeStatic(object2);
    }
}

