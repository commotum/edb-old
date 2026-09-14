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
import datomic.valcache.puts_pool$fn__9835$G__9828__9846;
import datomic.valcache.puts_pool$fn__9835$G__9829__9840;

public final class puts_pool$fn__9835
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        puts_pool$fn__9835$G__9829__9840 G__9829;
        puts_pool$fn__9835$G__9829__9840 puts_pool$fn__9835$G__9829__9840 = G__9829 = new puts_pool$fn__9835$G__9829__9840();
        G__9829 = null;
        puts_pool$fn__9835$G__9828__9846 f__7646__auto__9851 = new puts_pool$fn__9835$G__9828__9846((Object)puts_pool$fn__9835$G__9829__9840);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__9851).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__9851;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return puts_pool$fn__9835.invokeStatic(object2);
    }
}

