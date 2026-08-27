/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;
import datomic.db$memdb_needs_index_QMARK_$fn__13189;

public final class db$memdb_needs_index_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"memory-db");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"data-needs-index?");
    public static final Keyword const__2 = RT.keyword(null, (String)"aevt");
    public static final AFn const__5 = (AFn)Tuple.create((Object)15L);
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"remove");
    public static final AFn const__8 = (AFn)Tuple.create((Object)44L);

    public static Object invokeStatic(Object db2) {
        Object object;
        Object or__5238__auto__13192;
        Object mdb = ((IFn)const__0.getRawRoot()).invoke(db2);
        Object object2 = or__5238__auto__13192 = ((IFn)const__1.getRawRoot()).invoke(db2, ((Database)mdb).datoms(const__2, RT.object_array((Object)const__5)));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13192;
            or__5238__auto__13192 = null;
        } else {
            Object object3 = db2;
            Object object4 = db2;
            db2 = null;
            Object object5 = mdb;
            mdb = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, ((IFn)const__6.getRawRoot()).invoke((Object)new db$memdb_needs_index_QMARK_$fn__13189(object4), ((Database)object5).datoms(const__2, RT.object_array((Object)const__8))));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$memdb_needs_index_QMARK_.invokeStatic(object2);
    }
}

