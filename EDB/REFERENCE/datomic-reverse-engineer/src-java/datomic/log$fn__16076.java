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
import datomic.log$fn__16076$G__16071__16081;
import datomic.log$fn__16076$G__16072__16078;

public final class log$fn__16076
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        log$fn__16076$G__16072__16078 G__16072;
        log$fn__16076$G__16072__16078 log$fn__16076$G__16072__16078 = G__16072 = new log$fn__16076$G__16072__16078();
        G__16072 = null;
        log$fn__16076$G__16071__16081 f__7646__auto__16086 = new log$fn__16076$G__16071__16081((Object)log$fn__16076$G__16072__16078);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__16086).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__16086;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fn__16076.invokeStatic(object2);
    }
}

