/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Db;

public final class db$has_memory_index_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"mem-index-set");

    public static Object invokeStatic(Object db2) {
        Object object;
        Object or__5238__auto__13629;
        Object object2 = or__5238__auto__13629 = ((IFn)const__0.getRawRoot()).invoke((Object)(Util.identical((Object)((Db)db2).indexing, null) ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13629;
            or__5238__auto__13629 = null;
        } else {
            Object object3 = db2;
            db2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)(Util.equiv((Object)const__3.getRawRoot(), (Object)((Db)object3).memidx) ? Boolean.TRUE : Boolean.FALSE));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$has_memory_index_QMARK_.invokeStatic(object2);
    }
}

