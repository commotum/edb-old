/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLO
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

public final class caffeine$create_soft_limited
extends AFunction
implements IFn.LO,
IFn.LLO {
    public static final Var const__0 = RT.var((String)"datomic.cache.caffeine", (String)"adapt-caffeine-cache");

    public static Object invokeStatic(long num_entries, long l) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Caffeine.newBuilder().expireAfterAccess(l, TimeUnit.MINUTES).maximumSize(num_entries).softValues().build());
    }

    public Object invoke(Object object, Object object2) {
        return caffeine$create_soft_limited.invokeStatic(RT.longCast((Object)((Number)object)), RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(long l, long l2) {
        return caffeine$create_soft_limited.invokeStatic(l, l2);
    }

    public static Object invokeStatic(long num_entries) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Caffeine.newBuilder().maximumSize(num_entries).softValues().build());
    }

    public Object invoke(Object object) {
        return caffeine$create_soft_limited.invokeStatic(RT.longCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return caffeine$create_soft_limited.invokeStatic(l);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Caffeine.newBuilder().softValues().build());
    }

    public Object invoke() {
        return caffeine$create_soft_limited.invokeStatic();
    }
}

