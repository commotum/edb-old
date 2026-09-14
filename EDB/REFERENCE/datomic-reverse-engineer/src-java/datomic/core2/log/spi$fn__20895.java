/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.MethodImplCache
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.MethodImplCache;
import datomic.core2.log.spi$fn__20895$G__20890__20904;
import datomic.core2.log.spi$fn__20895$G__20891__20899;

public final class spi$fn__20895
extends AFunction {
    public static Object invokeStatic(Object cache__8036__auto__) {
        spi$fn__20895$G__20891__20899 G__20891;
        spi$fn__20895$G__20891__20899 spi$fn__20895$G__20891__20899 = G__20891 = new spi$fn__20895$G__20891__20899();
        G__20891 = null;
        spi$fn__20895$G__20890__20904 f__8037__auto__20909 = new spi$fn__20895$G__20890__20904((Object)spi$fn__20895$G__20891__20899);
        Object object = cache__8036__auto__;
        cache__8036__auto__ = null;
        ((AFunction)f__8037__auto__20909).__methodImplCache = (MethodImplCache)object;
        Object var2_2 = null;
        return f__8037__auto__20909;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return spi$fn__20895.invokeStatic(object2);
    }
}

