/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Map;

public final class db$expand_map$hook_entry_QMARK___13731
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__1 = (AFn)PersistentHashSet.create((Object[])new Object[]{"db.alter", "db.install"});
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"resolve-kw");

    public db$expand_map$hook_entry_QMARK___13731(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13730_SHARP_) {
        Object object = p1__13730_SHARP_;
        p1__13730_SHARP_ = null;
        db$expand_map$hook_entry_QMARK___13731 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(this_.db, ((Map.Entry)object).getKey())));
    }
}

