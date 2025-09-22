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
import datomic.valcache$fn__9714$G__9703__9719;
import datomic.valcache$fn__9714$G__9704__9716;

public final class valcache$fn__9714
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        valcache$fn__9714$G__9704__9716 G__9704;
        valcache$fn__9714$G__9704__9716 valcache$fn__9714$G__9704__9716 = G__9704 = new valcache$fn__9714$G__9704__9716();
        G__9704 = null;
        valcache$fn__9714$G__9703__9719 f__7646__auto__9724 = new valcache$fn__9714$G__9703__9719((Object)valcache$fn__9714$G__9704__9716);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9724).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9724;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$fn__9714.invokeStatic(object2);
    }
}

