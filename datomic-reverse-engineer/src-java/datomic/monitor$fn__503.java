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
import datomic.monitor$fn__503$G__498__508;
import datomic.monitor$fn__503$G__499__505;

public final class monitor$fn__503
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        monitor$fn__503$G__499__505 G__499;
        monitor$fn__503$G__499__505 monitor$fn__503$G__499__505 = G__499 = new monitor$fn__503$G__499__505();
        G__499 = null;
        monitor$fn__503$G__498__508 f__7646__auto__513 = new monitor$fn__503$G__498__508((Object)monitor$fn__503$G__499__505);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__513).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__513;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return monitor$fn__503.invokeStatic(object2);
    }
}

