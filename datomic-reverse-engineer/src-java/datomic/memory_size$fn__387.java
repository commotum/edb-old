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
import datomic.memory_size$fn__387$G__382__392;
import datomic.memory_size$fn__387$G__383__389;

public final class memory_size$fn__387
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        memory_size$fn__387$G__383__389 G__383;
        memory_size$fn__387$G__383__389 memory_size$fn__387$G__383__389 = G__383 = new memory_size$fn__387$G__383__389();
        G__383 = null;
        memory_size$fn__387$G__382__392 f__7646__auto__397 = new memory_size$fn__387$G__382__392((Object)memory_size$fn__387$G__383__389);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__397).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__397;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__387.invokeStatic(object2);
    }
}

