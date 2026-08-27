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
import datomic.cluster$fn__10523$G__10446__10530;
import datomic.cluster$fn__10523$G__10447__10526;

public final class cluster$fn__10523
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        cluster$fn__10523$G__10447__10526 G__10447;
        cluster$fn__10523$G__10447__10526 cluster$fn__10523$G__10447__10526 = G__10447 = new cluster$fn__10523$G__10447__10526();
        G__10447 = null;
        cluster$fn__10523$G__10446__10530 f__7646__auto__10535 = new cluster$fn__10523$G__10446__10530((Object)cluster$fn__10523$G__10447__10526);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__10535).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__10535;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$fn__10523.invokeStatic(object2);
    }
}

