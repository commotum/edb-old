/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.github.benmanes.caffeine.cache.Caffeine
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

public final class caffeine$create_response_map
extends AFunction
implements IFn.LO {
    public static final Var const__0 = RT.var((String)"datomic.cache.caffeine", (String)"adapt-caffeine-cache");

    public static Object invokeStatic(long timeout_minutes) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Caffeine.newBuilder().expireAfterWrite(timeout_minutes, TimeUnit.MINUTES).build());
    }

    public Object invoke(Object object) {
        return caffeine$create_response_map.invokeStatic(RT.longCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return caffeine$create_response_map.invokeStatic(l);
    }
}

