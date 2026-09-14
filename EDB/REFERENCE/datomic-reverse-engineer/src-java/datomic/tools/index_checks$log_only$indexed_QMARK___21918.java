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
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class index_checks$log_only$indexed_QMARK___21918
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"tx");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__10 = RT.keyword(null, (String)"eavt");

    public index_checks$log_only$indexed_QMARK___21918(Object object) {
        this.db = object;
    }

    public Object invoke(Object p__21917) {
        Object map__21919;
        Object object;
        Object object2 = p__21917;
        p__21917 = null;
        Object map__219192 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__219192);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__219192;
            map__219192 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__219192;
            map__219192 = null;
        }
        Object d = map__21919 = object;
        Object e = RT.get((Object)map__21919, (Object)const__3);
        Object a = RT.get((Object)map__21919, (Object)const__4);
        Object v = RT.get((Object)map__21919, (Object)const__5);
        Object object5 = map__21919;
        map__21919 = null;
        Object tx = RT.get((Object)object5, (Object)const__6);
        Object object6 = d;
        d = null;
        Object object7 = e;
        e = null;
        Object object8 = a;
        a = null;
        Object object9 = v;
        v = null;
        Object object10 = tx;
        tx = null;
        index_checks$log_only$indexed_QMARK___21918 this_ = null;
        return Util.equiv((Object)object6, (Object)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(this_.db, (Object)const__10, object7, object8, object9, object10))) ? Boolean.TRUE : Boolean.FALSE;
    }
}

