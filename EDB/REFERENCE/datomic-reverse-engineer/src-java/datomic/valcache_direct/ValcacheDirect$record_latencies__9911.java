/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.valcache_direct;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValcacheDirect$record_latencies__9911
extends AFunction {
    public static final Keyword const__3 = RT.keyword(null, (String)"ValcacheGetSucceededNsec");
    public static final Keyword const__5 = RT.keyword(null, (String)"ValcacheGetMissedNsec");
    public static final Keyword const__6 = RT.keyword(null, (String)"ValcacheGetExceptionNsec");
    public static final Var const__7 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Var const__8 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__9 = RT.keyword(null, (String)"valcache-ns");

    public Object invoke(Object nanos, Object p__9910) {
        Keyword k;
        Keyword keyword;
        Object object = p__9910;
        p__9910 = null;
        Object vec__9912 = object;
        Object v = RT.nth((Object)vec__9912, (int)RT.intCast((long)0L), null);
        Object object2 = vec__9912;
        vec__9912 = null;
        Object ex = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = v;
        v = null;
        if (object3 != null && object3 != Boolean.FALSE) {
            keyword = const__3;
        } else {
            Object object4 = ex;
            ex = null;
            keyword = Util.identical((Object)object4, null) ? const__5 : const__6;
        }
        Keyword keyword2 = k = keyword;
        k = null;
        ((IFn)const__7.getRawRoot()).invoke((Object)keyword2, nanos);
        Object object5 = nanos;
        nanos = null;
        return ((IFn.OLO)const__8.getRawRoot()).invokePrim((Object)const__9, RT.longCast((Object)((Number)object5)));
    }
}

