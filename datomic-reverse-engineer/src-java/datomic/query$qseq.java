/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$qseq$f__19556;

public final class query$qseq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"offset");
    public static final Object const__4 = 0L;
    public static final Keyword const__5 = RT.keyword(null, (String)"limit");
    public static final Keyword const__6 = RT.keyword(null, (String)"io-context");
    public static final Var const__7 = RT.var((String)"datomic.measure.io-stats", (String)"throw-if-ex!");
    public static final Var const__8 = RT.var((String)"datomic.measure.io-stats", (String)"with-io-stats");
    public static final Keyword const__9 = RT.keyword(null, (String)"api");
    public static final Keyword const__10 = RT.keyword(null, (String)"qseq");
    public static final Keyword const__11 = RT.keyword(null, (String)"ret");
    public static final Keyword const__12 = RT.keyword(null, (String)"io-stats");
    public static final Var const__15 = RT.var((String)"datomic.common", (String)"result-xform");
    public static final Var const__16 = RT.var((String)"datomic.query.support", (String)"counted-seq");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"sequence");
    public static final Var const__18 = RT.var((String)"datomic.common", (String)"result-count");
    public static final Var const__19 = RT.var((String)"datomic.query", (String)"qseq");
    public static final Keyword const__20 = RT.keyword(null, (String)"query");
    public static final Keyword const__21 = RT.keyword(null, (String)"args");

    public static Object invokeStatic(Object query_map2, Object args) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__20;
        Object object = query_map2;
        query_map2 = null;
        objectArray[1] = object;
        objectArray[2] = const__21;
        Object object2 = args;
        args = null;
        objectArray[3] = object2;
        return ((IFn)const__19.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$qseq.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object p__19550) {
        Object object;
        Object xform;
        Object pf;
        Object ret;
        Object object2;
        PersistentHashMap persistentHashMap;
        Object map__19551;
        Object object3;
        Object object4 = p__19550;
        p__19550 = null;
        Object map__195512 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__195512);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__195512;
            map__195512 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__195512;
            map__195512 = null;
        }
        Object query_map2 = map__19551 = object3;
        Object offset = RT.get((Object)map__19551, (Object)const__3, (Object)const__4);
        Object limit2 = RT.get((Object)map__19551, (Object)const__5, (Object)Numbers.num((long)Long.MAX_VALUE));
        Object object7 = map__19551;
        map__19551 = null;
        Object io_context = RT.get((Object)object7, (Object)const__6);
        Object object8 = query_map2;
        query_map2 = null;
        query$qseq$f__19556 f = new query$qseq$f__19556(object8);
        Object object9 = io_context;
        PersistentHashMap map__19552 = object9 != null && object9 != Boolean.FALSE ? ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)f, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__6, io_context, const__9, const__10}))) : null;
        Object object10 = ((IFn)const__0.getRawRoot()).invoke(map__19552);
        if (object10 != null && object10 != Boolean.FALSE) {
            PersistentHashMap persistentHashMap2 = map__19552;
            map__19552 = null;
            persistentHashMap = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(persistentHashMap2)));
        } else {
            persistentHashMap = map__19552;
            map__19552 = null;
        }
        PersistentHashMap map__195522 = persistentHashMap;
        Object ret2 = RT.get(map__195522, (Object)const__11);
        PersistentHashMap persistentHashMap3 = map__195522;
        map__195522 = null;
        Object io_stats = RT.get((Object)persistentHashMap3, (Object)const__12);
        Object object11 = io_context;
        if (object11 != null && object11 != Boolean.FALSE) {
            object2 = ret2;
            ret2 = null;
        } else {
            query$qseq$f__19556 query$qseq$f__19556 = f;
            f = null;
            object2 = ((IFn)query$qseq$f__19556).invoke();
        }
        Object object12 = ret = object2;
        ret = null;
        Object vec__19553 = object12;
        Object result2 = RT.nth((Object)vec__19553, (int)RT.intCast((long)0L), null);
        Object object13 = vec__19553;
        vec__19553 = null;
        Object object14 = pf = RT.nth((Object)object13, (int)RT.intCast((long)1L), null);
        pf = null;
        Object object15 = xform = ((IFn)const__15.getRawRoot()).invoke(offset, limit2, object14);
        xform = null;
        Object object16 = ((IFn)const__17.getRawRoot()).invoke(object15, result2);
        Object object17 = offset;
        offset = null;
        Object object18 = limit2;
        limit2 = null;
        Object object19 = result2;
        result2 = null;
        Object cseq = ((IFn)const__16.getRawRoot()).invoke(object16, ((IFn)const__18.getRawRoot()).invoke(object17, object18, object19));
        Object object20 = io_context;
        io_context = null;
        if (object20 != null && object20 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__11;
            Object object21 = cseq;
            cseq = null;
            objectArray[1] = object21;
            objectArray[2] = const__12;
            Object object22 = io_stats;
            io_stats = null;
            objectArray[3] = object22;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = cseq;
            cseq = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$qseq.invokeStatic(object2);
    }
}

