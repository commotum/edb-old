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
import datomic.backup$fn__19978$G__19941__19987;
import datomic.backup$fn__19978$G__19942__19982;

public final class backup$fn__19978
extends AFunction {
    public static Object invokeStatic(Object cache__7645__auto__) {
        backup$fn__19978$G__19942__19982 G__19942;
        backup$fn__19978$G__19942__19982 backup$fn__19978$G__19942__19982 = G__19942 = new backup$fn__19978$G__19942__19982();
        G__19942 = null;
        backup$fn__19978$G__19941__19987 f__7646__auto__19992 = new backup$fn__19978$G__19941__19987((Object)backup$fn__19978$G__19942__19982);
        Object object = cache__7645__auto__;
        cache__7645__auto__ = null;
        ((AFunction)f__7646__auto__19992).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__7646__auto__19992;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__19978.invokeStatic(object2);
    }
}

