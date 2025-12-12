/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$init_db$assert_kw__14140;
import datomic.db.Db;

public final class db$init_db
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"load-builtins");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"mem-index-set");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"memlog");
    public static final Object const__4 = 0L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"BOOT-IDS");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"add-fulltext");
    public static final Keyword const__9 = RT.keyword(null, (String)"schema-level");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"MAX_SCHEMA_LEVEL");
    public static final Keyword const__11 = RT.keyword(null, (String)"birth-level");

    public static Object invokeStatic(Object id) {
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        db$init_db$assert_kw__14140 assert_kw = new db$init_db$assert_kw__14140();
        Object object = id;
        id = null;
        Db db2 = new Db(object, const__2.getRawRoot(), null, null, null, null, ((IFn)const__3.getRawRoot()).invoke(), 0L, 0L, 0L, null, PersistentVector.EMPTY, PersistentArrayMap.EMPTY, PersistentArrayMap.EMPTY, null, const__4, null, null, null, null);
        db$init_db$assert_kw__14140 db$init_db$assert_kw__14140 = assert_kw;
        assert_kw = null;
        Object data2 = ((IFn)const__5.getRawRoot()).invoke((Object)db$init_db$assert_kw__14140, const__6.getRawRoot());
        Db db3 = db2;
        db2 = null;
        Object object2 = db3.acceptData(data2);
        Object object3 = data2;
        data2 = null;
        return ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(object2, object3), (Object)const__9, const__10.getRawRoot(), (Object)const__11, const__10.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$init_db.invokeStatic(object2);
    }
}

