/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class fressian$record_latencies
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__1 = RT.keyword(null, (String)"deserialize");
    public static final Var const__2 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__3 = RT.keyword(null, (String)"DeserializeNsec");
    public static final Keyword const__4 = RT.keyword(null, (String)"deserialize-ns");

    public static Object invokeStatic(Object nanos, Object _) {
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        ((IFn)const__2.getRawRoot()).invoke((Object)const__3, nanos);
        Object object = nanos;
        nanos = null;
        return ((IFn.OLO)const__0.getRawRoot()).invokePrim((Object)const__4, RT.longCast((Object)((Number)object)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$record_latencies.invokeStatic(object3, object4);
    }
}

