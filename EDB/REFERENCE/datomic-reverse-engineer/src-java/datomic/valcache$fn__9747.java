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
import datomic.valcache$fn__9747$G__9705__9752;
import datomic.valcache$fn__9747$G__9706__9749;

public final class valcache$fn__9747
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        valcache$fn__9747$G__9706__9749 G__9706;
        valcache$fn__9747$G__9706__9749 valcache$fn__9747$G__9706__9749 = G__9706 = new valcache$fn__9747$G__9706__9749();
        G__9706 = null;
        valcache$fn__9747$G__9705__9752 f__7646__auto__9757 = new valcache$fn__9747$G__9705__9752((Object)valcache$fn__9747$G__9706__9749);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9757).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9757;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$fn__9747.invokeStatic(object2);
    }
}

