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
import datomic.valcache$fn__9725$G__9707__9730;
import datomic.valcache$fn__9725$G__9708__9727;

public final class valcache$fn__9725
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        valcache$fn__9725$G__9708__9727 G__9708;
        valcache$fn__9725$G__9708__9727 valcache$fn__9725$G__9708__9727 = G__9708 = new valcache$fn__9725$G__9708__9727();
        G__9708 = null;
        valcache$fn__9725$G__9707__9730 f__7646__auto__9735 = new valcache$fn__9725$G__9707__9730((Object)valcache$fn__9725$G__9708__9727);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9735).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9735;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$fn__9725.invokeStatic(object2);
    }
}

