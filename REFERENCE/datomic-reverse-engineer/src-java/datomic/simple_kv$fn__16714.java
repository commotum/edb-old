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
import datomic.simple_kv$fn__16714$G__16679__16721;
import datomic.simple_kv$fn__16714$G__16680__16717;

public final class simple_kv$fn__16714
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        simple_kv$fn__16714$G__16680__16717 G__16680;
        simple_kv$fn__16714$G__16680__16717 simple_kv$fn__16714$G__16680__16717 = G__16680 = new simple_kv$fn__16714$G__16680__16717();
        G__16680 = null;
        simple_kv$fn__16714$G__16679__16721 f__7646__auto__16726 = new simple_kv$fn__16714$G__16679__16721((Object)simple_kv$fn__16714$G__16680__16717);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__16726).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__16726;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return simple_kv$fn__16714.invokeStatic(object2);
    }
}

