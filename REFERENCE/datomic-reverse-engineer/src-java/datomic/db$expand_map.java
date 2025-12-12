/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$expand_map$fn__13734;
import datomic.db$expand_map$hook_entry_QMARK___13731;
import datomic.db.LocalizeTempid;

public final class db$expand_map
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object db, Object m, Object part_reqs, Object local_tempids) {
        v0 = or__5238__auto__13744 = RT.get((Object)m, (Object)db$expand_map.const__2);
        if (v0 != null && v0 != Boolean.FALSE) {
            v1 = or__5238__auto__13744;
            or__5238__auto__13744 = null;
        } else {
            v1 = v2 = ((IFn.LO)db$expand_map.const__3.getRawRoot()).invokePrim(16L);
        }
        if (Util.classOf((Object)v1) == db$expand_map.__cached_class__0) ** GOTO lbl11
        if (!(v2 instanceof LocalizeTempid)) {
            v2 = v2;
            db$expand_map.__cached_class__0 = Util.classOf((Object)v2);
lbl11:
            // 2 sources

            v3 = db$expand_map.const__0.getRawRoot().invoke(v2, db, null, local_tempids);
        } else {
            v3 = ((LocalizeTempid)v2).local_id(db, null, local_tempids);
        }
        dbid = v3;
        hook_entry_QMARK_ = new db$expand_map$hook_entry_QMARK___13731(db);
        v4 = dbid;
        dbid = null;
        v5 = db;
        db = null;
        v6 = local_tempids;
        local_tempids = null;
        v7 = part_reqs;
        part_reqs = null;
        v8 = ((IFn)db$expand_map.const__7.getRawRoot()).invoke((Object)hook_entry_QMARK_, m);
        v9 = hook_entry_QMARK_;
        hook_entry_QMARK_ = null;
        v10 = m;
        m = null;
        return ((IFn)db$expand_map.const__5.getRawRoot()).invoke((Object)new db$expand_map$fn__13734(v4, v5, v6, v7), (Object)PersistentVector.EMPTY, ((IFn)db$expand_map.const__6.getRawRoot()).invoke(v8, ((IFn)db$expand_map.const__8.getRawRoot()).invoke((Object)v9, v10)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$expand_map.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"local-id");
        const__2 = RT.keyword((String)"db", (String)"id");
        const__3 = RT.var((String)"datomic.db", (String)"tempid");
        const__5 = RT.var((String)"clojure.core", (String)"reduce");
        const__6 = RT.var((String)"clojure.core", (String)"concat");
        const__7 = RT.var((String)"clojure.core", (String)"remove");
        const__8 = RT.var((String)"clojure.core", (String)"filter");
    }
}

