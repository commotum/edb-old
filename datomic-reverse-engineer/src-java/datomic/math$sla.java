/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class math$sla
extends AFunction {
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"sorted-map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"last");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object sorted_coll) {
        double percentile = 0.9;
        Object object = sorted_coll;
        sorted_coll = null;
        Object coll = object;
        Object results = PersistentArrayMap.EMPTY;
        while (true) {
            Object rest;
            if ((long)RT.count((Object)coll) < 10L) break;
            Double d = Numbers.multiply((double)0.9, (long)RT.count((Object)coll));
            Object object2 = coll;
            coll = null;
            Object object3 = rest = ((IFn)const__8.getRawRoot()).invoke((Object)d, object2);
            PersistentArrayMap persistentArrayMap = results;
            results = null;
            Object object4 = rest;
            rest = null;
            results = ((IFn)const__6.getRawRoot()).invoke((Object)persistentArrayMap, (Object)percentile, ((IFn)const__15.getRawRoot()).invoke(object4));
            coll = object3;
            percentile += 0.9 * Numbers.minus((long)1L, (double)percentile);
        }
        PersistentArrayMap persistentArrayMap = results;
        results = null;
        Object object5 = coll;
        coll = null;
        return ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(), ((IFn)const__6.getRawRoot()).invoke((Object)persistentArrayMap, (Object)percentile, ((IFn)const__7.getRawRoot()).invoke(object5)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$sla.invokeStatic(object2);
    }
}

