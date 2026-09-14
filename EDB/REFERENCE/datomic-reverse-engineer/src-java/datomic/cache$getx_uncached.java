/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cache.ICachedLookup;

public final class cache$getx_uncached
extends AFunction {
    public static final Keyword const__2 = RT.keyword((String)"datomic.cache", (String)"getx-sentinel-42");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"getx");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object m, Object k) {
        Object object;
        if (m instanceof ICachedLookup) {
            Object object2 = m;
            m = null;
            Object e = ((ICachedLookup)object2).valAtUncached(k, const__2);
            Object object3 = ((IFn)const__3.getRawRoot()).invoke((Object)(Util.equiv((Object)e, (Object)const__2) ? Boolean.TRUE : Boolean.FALSE));
            if (object3 != null && object3 != Boolean.FALSE) {
                object = e;
                return object;
            }
            Object object4 = k;
            k = null;
            throw (Throwable)new Exception((String)((IFn)const__5.getRawRoot()).invoke((Object)"Key not found: ", object4));
        }
        Object object5 = m;
        m = null;
        Object object6 = k;
        k = null;
        object = ((IFn)const__6.getRawRoot()).invoke(object5, object6);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cache$getx_uncached.invokeStatic(object3, object4);
    }
}

