/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;

public final class integrity$crosscheck_eavt_aevt
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"mk-index-pred");
    public static final Keyword const__1 = RT.keyword(null, (String)"aevt");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datum");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"eavt");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"aevt");
    public static final Keyword const__11 = RT.keyword(null, (String)"datom");
    public static final Keyword const__12 = RT.keyword(null, (String)"db2");
    public static final Keyword const__13 = RT.keyword(null, (String)"db1");
    public static final Keyword const__15 = RT.keyword(null, (String)"check");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"crosscheck-eavt-aevt");
    public static final Keyword const__17 = RT.keyword(null, (String)"next-t");
    public static final Keyword const__18 = RT.keyword(null, (String)"count");
    public static final Keyword const__19 = RT.keyword(null, (String)"msec");

    public static Object invokeStatic(Object db1, Object db2, Object progress) {
        long start = System.nanoTime();
        Object index_pred = ((IFn)const__0.getRawRoot()).invoke(db2, (Object)const__1);
        Object i = ((IFn)const__2.getRawRoot()).invoke((Object)((IDb)db1).seekEAVT((IDatum)((IFn)const__3.getRawRoot()).invoke(db1)));
        long c = 0L;
        while (true) {
            ((IFn)progress).invoke((Object)Numbers.num((long)c));
            Object object = i;
            if (object == null || object == Boolean.FALSE) break;
            Object datum2 = ((Iter)i).get();
            Object object2 = ((IFn)index_pred).invoke(datum2);
            if (object2 != null && object2 != Boolean.FALSE) {
                if (!Util.equiv((Object)((IDb)db2).seekAEVT((IDatum)datum2).get(), (Object)datum2)) {
                    Object object3 = ((IFn)const__7.getRawRoot()).invoke((Object)"Found ", ((IFn)const__8.getRawRoot()).invoke(datum2), (Object)" in ", (Object)const__9, (Object)" but not in ", (Object)const__10);
                    Object[] objectArray = new Object[6];
                    objectArray[0] = const__11;
                    Object object4 = datum2;
                    datum2 = null;
                    objectArray[1] = object4;
                    objectArray[2] = const__12;
                    objectArray[3] = db2;
                    objectArray[4] = const__13;
                    objectArray[5] = db2;
                    throw (Throwable)((IFn)const__6.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                Object object5 = i;
                i = null;
                c = Numbers.inc((long)c);
                i = ((Iter)object5).next();
                continue;
            }
            Object object6 = i;
            i = null;
            i = ((Iter)object6).next();
        }
        return RT.mapUniqueKeys((Object[])new Object[]{const__15, const__16, const__17, ((IDb)db1).getNextT(), const__18, Numbers.num((long)c), const__19, Numbers.num((long)Numbers.quotient((long)Numbers.minus((long)System.nanoTime(), (long)start), (long)1000000L))});
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$crosscheck_eavt_aevt.invokeStatic(object4, object5, object6);
    }
}

