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
import datomic.cluster$fn__10472$G__10448__10483;
import datomic.cluster$fn__10472$G__10449__10477;

public final class cluster$fn__10472
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        cluster$fn__10472$G__10449__10477 G__10449;
        cluster$fn__10472$G__10449__10477 cluster$fn__10472$G__10449__10477 = G__10449 = new cluster$fn__10472$G__10449__10477();
        G__10449 = null;
        cluster$fn__10472$G__10448__10483 f__7646__auto__10488 = new cluster$fn__10472$G__10448__10483((Object)cluster$fn__10472$G__10449__10477);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10488).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10488;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$fn__10472.invokeStatic(object2);
    }
}

