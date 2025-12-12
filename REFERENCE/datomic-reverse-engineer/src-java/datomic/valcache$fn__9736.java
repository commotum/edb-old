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
import datomic.valcache$fn__9736$G__9709__9741;
import datomic.valcache$fn__9736$G__9710__9738;

public final class valcache$fn__9736
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        valcache$fn__9736$G__9710__9738 G__9710;
        valcache$fn__9736$G__9710__9738 valcache$fn__9736$G__9710__9738 = G__9710 = new valcache$fn__9736$G__9710__9738();
        G__9710 = null;
        valcache$fn__9736$G__9709__9741 f__7646__auto__9746 = new valcache$fn__9736$G__9709__9741((Object)valcache$fn__9736$G__9710__9738);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9746).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9746;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$fn__9736.invokeStatic(object2);
    }
}

