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
import datomic.backup$fn__20072$G__20052__20081;
import datomic.backup$fn__20072$G__20053__20076;

public final class backup$fn__20072
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        backup$fn__20072$G__20053__20076 G__20053;
        backup$fn__20072$G__20053__20076 backup$fn__20072$G__20053__20076 = G__20053 = new backup$fn__20072$G__20053__20076();
        G__20053 = null;
        backup$fn__20072$G__20052__20081 f__7646__auto__20086 = new backup$fn__20072$G__20052__20081((Object)backup$fn__20072$G__20053__20076);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__20086).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__20086;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__20072.invokeStatic(object2);
    }
}

