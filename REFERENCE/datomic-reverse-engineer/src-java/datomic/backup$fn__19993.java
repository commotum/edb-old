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
import datomic.backup$fn__19993$G__19947__20000;
import datomic.backup$fn__19993$G__19948__19996;

public final class backup$fn__19993
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        backup$fn__19993$G__19948__19996 G__19948;
        backup$fn__19993$G__19948__19996 backup$fn__19993$G__19948__19996 = G__19948 = new backup$fn__19993$G__19948__19996();
        G__19948 = null;
        backup$fn__19993$G__19947__20000 f__7646__auto__20005 = new backup$fn__19993$G__19947__20000((Object)backup$fn__19993$G__19948__19996);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__20005).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__20005;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__19993.invokeStatic(object2);
    }
}

