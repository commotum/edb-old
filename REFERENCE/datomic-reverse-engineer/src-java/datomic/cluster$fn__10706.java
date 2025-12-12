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
import datomic.cluster$fn__10706$G__10690__10711;
import datomic.cluster$fn__10706$G__10691__10708;

public final class cluster$fn__10706
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        cluster$fn__10706$G__10691__10708 G__10691;
        cluster$fn__10706$G__10691__10708 cluster$fn__10706$G__10691__10708 = G__10691 = new cluster$fn__10706$G__10691__10708();
        G__10691 = null;
        cluster$fn__10706$G__10690__10711 f__7646__auto__10716 = new cluster$fn__10706$G__10690__10711((Object)cluster$fn__10706$G__10691__10708);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10716).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10716;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$fn__10706.invokeStatic(object2);
    }
}

