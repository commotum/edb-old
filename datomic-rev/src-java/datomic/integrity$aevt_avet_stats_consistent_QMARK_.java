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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class integrity$aevt_avet_stats_consistent_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"aevt-total");
    public static final Keyword const__4 = RT.keyword(null, (String)"avet-total");

    public static Object invokeStatic(Object p__22488) {
        Object object;
        Object object2 = p__22488;
        p__22488 = null;
        Object map__22489 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__22489);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__22489;
            map__22489 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__22489;
            map__22489 = null;
        }
        Object map__224892 = object;
        Object aevt_total = RT.get((Object)map__224892, (Object)const__3);
        Object object5 = map__224892;
        map__224892 = null;
        Object avet_total = RT.get((Object)object5, (Object)const__4);
        Object object6 = aevt_total;
        aevt_total = null;
        Object object7 = avet_total;
        avet_total = null;
        return Util.equiv((Object)object6, (Object)object7) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$aevt_avet_stats_consistent_QMARK_.invokeStatic(object2);
    }
}

