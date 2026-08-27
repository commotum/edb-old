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
import datomic.log$fn__16008$G__15982__16013;
import datomic.log$fn__16008$G__15983__16010;

public final class log$fn__16008
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        log$fn__16008$G__15983__16010 G__15983;
        log$fn__16008$G__15983__16010 log$fn__16008$G__15983__16010 = G__15983 = new log$fn__16008$G__15983__16010();
        G__15983 = null;
        log$fn__16008$G__15982__16013 f__7646__auto__16018 = new log$fn__16008$G__15982__16013((Object)log$fn__16008$G__15983__16010);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__16018).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__16018;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fn__16008.invokeStatic(object2);
    }
}

