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
import datomic.log$fn__16030$G__15984__16041;
import datomic.log$fn__16030$G__15985__16035;

public final class log$fn__16030
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        log$fn__16030$G__15985__16035 G__15985;
        log$fn__16030$G__15985__16035 log$fn__16030$G__15985__16035 = G__15985 = new log$fn__16030$G__15985__16035();
        G__15985 = null;
        log$fn__16030$G__15984__16041 f__7646__auto__16046 = new log$fn__16030$G__15984__16041((Object)log$fn__16030$G__15985__16035);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__16046).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__16046;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fn__16030.invokeStatic(object2);
    }
}

