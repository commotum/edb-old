/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.nio.Buffer;

public final class valcache_direct$fits_in_cache_QMARK_
extends AFunction {
    public static Object invokeStatic(Object v) {
        Object object = v;
        v = null;
        return Numbers.lte((long)((Buffer)object).remaining(), (long)1000000L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache_direct$fits_in_cache_QMARK_.invokeStatic(object2);
    }
}

