/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.monitor;

import clojure.lang.AFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.monitor.Statistics$reify__523;
import datomic.monitor.Statistics$reify__525;
import datomic.monitor.Statistics$reify__527;
import datomic.monitor.Statistics$reify__529;
import datomic.monitor.StatsUpdate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

public final class Statistics
implements StatsUpdate,
IType {
    public final Object lo;
    public final Object hi;
    public final Object sum;
    public final Object count;
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 49, RT.keyword(null, (String)"column"), 42});
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 58, RT.keyword(null, (String)"column"), 42});
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 66, RT.keyword(null, (String)"column"), 36});
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 74, RT.keyword(null, (String)"column"), 36});

    public Statistics(Object object, Object object2, Object object3, Object object4) {
        this.lo = object;
        this.hi = object2;
        this.sum = object3;
        this.count = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"lo")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentHashMap")})), (Object)((IObj)Symbol.intern(null, (String)"hi")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentHashMap")})), (Object)((IObj)Symbol.intern(null, (String)"sum")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentHashMap")})), (Object)((IObj)Symbol.intern(null, (String)"count")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentHashMap")})));
    }

    public Object addObservation(Object k, Object v) {
        Object v2;
        Object or__5238__auto__535;
        Object v3;
        Object or__5238__auto__534;
        Object v4;
        Object or__5238__auto__533;
        Object v5;
        Object or__5238__auto__532;
        Object v6 = or__5238__auto__532 = ((ConcurrentHashMap)this.lo).get(k);
        if (v6 != null && v6 != Boolean.FALSE) {
            v5 = or__5238__auto__532;
            or__5238__auto__532 = null;
        } else {
            v5 = ((ConcurrentHashMap)this.lo).computeIfAbsent(k, (Function)((IObj)new Statistics$reify__523(null)).withMeta((IPersistentMap)const__4));
        }
        ((LongAccumulator)v5).accumulate(RT.longCast((Object)((Number)v)));
        Object v7 = or__5238__auto__533 = ((ConcurrentHashMap)this.hi).get(k);
        if (v7 != null && v7 != Boolean.FALSE) {
            v4 = or__5238__auto__533;
            or__5238__auto__533 = null;
        } else {
            v4 = ((ConcurrentHashMap)this.hi).computeIfAbsent(k, (Function)((IObj)new Statistics$reify__525(null)).withMeta((IPersistentMap)const__6));
        }
        ((LongAccumulator)v4).accumulate(RT.longCast((Object)((Number)v)));
        Object v8 = or__5238__auto__534 = ((ConcurrentHashMap)this.sum).get(k);
        if (v8 != null && v8 != Boolean.FALSE) {
            v3 = or__5238__auto__534;
            or__5238__auto__534 = null;
        } else {
            v3 = ((ConcurrentHashMap)this.sum).computeIfAbsent(k, (Function)((IObj)new Statistics$reify__527(null)).withMeta((IPersistentMap)const__9));
        }
        Object object = v;
        v = null;
        ((LongAdder)v3).add(RT.longCast((Object)((Number)object)));
        Object v9 = or__5238__auto__535 = ((ConcurrentHashMap)this.count).get(k);
        if (v9 != null && v9 != Boolean.FALSE) {
            v2 = or__5238__auto__535;
            or__5238__auto__535 = null;
        } else {
            Object object2 = k;
            k = null;
            v2 = ((ConcurrentHashMap)this.count).computeIfAbsent(object2, (Function)((IObj)new Statistics$reify__529(null)).withMeta((IPersistentMap)const__11));
        }
        ((LongAdder)v2).increment();
        return null;
    }
}

