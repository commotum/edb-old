/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.MethodImplCache
 */
package datomic.valcache;

import clojure.lang.AFunction;
import clojure.lang.MethodImplCache;
import datomic.valcache.puts_pool$fn__9852$G__9830__9859;
import datomic.valcache.puts_pool$fn__9852$G__9831__9855;

public final class puts_pool$fn__9852
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        puts_pool$fn__9852$G__9831__9855 G__9831;
        puts_pool$fn__9852$G__9831__9855 puts_pool$fn__9852$G__9831__9855 = G__9831 = new puts_pool$fn__9852$G__9831__9855();
        G__9831 = null;
        puts_pool$fn__9852$G__9830__9859 f__7646__auto__9864 = new puts_pool$fn__9852$G__9830__9859((Object)puts_pool$fn__9852$G__9831__9855);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9864).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9864;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return puts_pool$fn__9852.invokeStatic(object2);
    }
}

