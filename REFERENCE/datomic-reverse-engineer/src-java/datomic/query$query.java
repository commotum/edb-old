/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$query$f__19543;
import datomic.query$query$fn__19545;
import datomic.query$query$fn__19547;

public final class query$query
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"io-context");
    public static final Keyword const__4 = RT.keyword(null, (String)"query-stats");

    public static Object invokeStatic(Object p__19541) {
        AFunction f;
        AFunction aFunction;
        AFunction aFunction2;
        Object map__19542;
        Object object;
        Object object2 = p__19541;
        p__19541 = null;
        Object map__195422 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__195422);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__195422;
            map__195422 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__195422;
            map__195422 = null;
        }
        Object query_map2 = map__19542 = object;
        Object io_context = RT.get((Object)map__19542, (Object)const__3);
        Object object5 = map__19542;
        map__19542 = null;
        Object query_stats = RT.get((Object)object5, (Object)const__4);
        query$query$f__19543 f2 = new query$query$f__19543(query_map2);
        Object object6 = io_context;
        if (object6 != null && object6 != Boolean.FALSE) {
            io_context = null;
            f2 = null;
            aFunction2 = new query$query$fn__19545(io_context, (Object)f2);
        } else {
            aFunction2 = f2;
            f2 = null;
        }
        AFunction f3 = aFunction2;
        Object object7 = query_stats;
        query_stats = null;
        if (object7 != null && object7 != Boolean.FALSE) {
            f3 = null;
            query_map2 = null;
            aFunction = new query$query$fn__19547(f3, query_map2);
        } else {
            aFunction = f3;
            f3 = null;
        }
        AFunction aFunction3 = f = aFunction;
        f = null;
        return ((IFn)aFunction3).invoke();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$query.invokeStatic(object2);
    }
}

