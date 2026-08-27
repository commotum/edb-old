/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cache$read_ahead$fn__9384;

public final class cache$read_ahead
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.cache", (String)"read-ahead-pool-prop");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"with-bindings*");
    public static final Var const__5 = RT.var((String)"datomic.measure.io-stats", (String)"bare-bindings");

    public static Object invokeStatic(Object lookup, Object k) {
        Object object;
        if (Numbers.lt((long)0L, (Object)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot()))) {
            Object object2 = lookup;
            lookup = null;
            Object object3 = k;
            k = null;
            object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(), (Object)new cache$read_ahead$fn__9384(object2, object3));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cache$read_ahead.invokeStatic(object3, object4);
    }
}

