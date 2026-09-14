/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$with_tx_PLUS_opts$f__14109;
import datomic.db$with_tx_PLUS_opts$fn__14111;

public final class db$with_tx_PLUS_opts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"return-hints");
    public static final Keyword const__4 = RT.keyword(null, (String)"io-context");
    public static final Keyword const__5 = RT.keyword((String)"datomic.db", (String)"trace");
    public static final Keyword const__6 = RT.keyword(null, (String)"api");
    public static final Keyword const__7 = RT.keyword(null, (String)"with");
    public static final Var const__8 = RT.var((String)"datomic.measure.io-stats", (String)"throw-if-ex!");
    public static final Var const__9 = RT.var((String)"datomic.measure.io-stats", (String)"with-io-stats");
    public static final Keyword const__10 = RT.keyword(null, (String)"ret");
    public static final Keyword const__11 = RT.keyword(null, (String)"io-stats");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__14 = RT.keyword(null, (String)"tx-stats");

    public static Object invokeStatic(Object db2, Object txdata, Object p__14107) {
        Object ret;
        Object object;
        Object object2;
        Object or__5238__auto__14118;
        AFunction aFunction;
        Object object3;
        Object object4 = p__14107;
        p__14107 = null;
        Object map__14108 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__14108);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__14108;
            map__14108 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__14108;
            map__14108 = null;
        }
        Object map__141082 = object3;
        Object return_hints = RT.get((Object)map__141082, (Object)const__3);
        Object object7 = map__141082;
        map__141082 = null;
        Object io_context = RT.get((Object)object7, (Object)const__4);
        Object object8 = db2;
        db2 = null;
        Object object9 = txdata;
        txdata = null;
        db$with_tx_PLUS_opts$f__14109 f = new db$with_tx_PLUS_opts$f__14109(object8, object9);
        Object object10 = return_hints;
        if (object10 != null && object10 != Boolean.FALSE) {
            f = null;
            aFunction = new db$with_tx_PLUS_opts$fn__14111((Object)f);
        } else {
            aFunction = f;
            f = null;
        }
        AFunction f2 = aFunction;
        Object object11 = return_hints;
        return_hints = null;
        Object object12 = or__5238__auto__14118 = object11;
        if (object12 != null && object12 != Boolean.FALSE) {
            object2 = or__5238__auto__14118;
            or__5238__auto__14118 = null;
        } else {
            object2 = io_context;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object13;
            Object object14;
            Object or__5238__auto__14119;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__4;
            Object object15 = io_context;
            io_context = null;
            Object object16 = or__5238__auto__14119 = object15;
            if (object16 != null && object16 != Boolean.FALSE) {
                object14 = or__5238__auto__14119;
                or__5238__auto__14119 = null;
            } else {
                object14 = const__5;
            }
            objectArray[1] = object14;
            objectArray[2] = const__6;
            objectArray[3] = const__7;
            IPersistentMap ctx = RT.mapUniqueKeys((Object[])objectArray);
            AFunction aFunction2 = f2;
            f2 = null;
            IPersistentMap iPersistentMap = ctx;
            ctx = null;
            Object map__14116 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)aFunction2, (Object)iPersistentMap));
            Object object17 = ((IFn)const__0.getRawRoot()).invoke(map__14116);
            if (object17 != null && object17 != Boolean.FALSE) {
                Object object18 = map__14116;
                map__14116 = null;
                object13 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object18)));
            } else {
                object13 = map__14116;
                map__14116 = null;
            }
            Object map__141162 = object13;
            Object ret2 = RT.get((Object)map__141162, (Object)const__10);
            Object object19 = map__141162;
            map__141162 = null;
            Object io_stats = RT.get((Object)object19, (Object)const__11);
            Object object20 = ret2;
            ret2 = null;
            Object object21 = io_stats;
            io_stats = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object20, (Object)const__11, object21);
        } else {
            AFunction aFunction3 = f2;
            f2 = null;
            object = ((IFn)aFunction3).invoke();
        }
        Object object22 = ret = object;
        ret = null;
        return ((IFn)const__13.getRawRoot()).invoke(object22, (Object)const__14);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$with_tx_PLUS_opts.invokeStatic(object4, object5, object6);
    }
}

