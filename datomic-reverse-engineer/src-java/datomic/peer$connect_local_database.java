/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class peer$connect_local_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.peer", (String)"local-dbs");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"db-not-found");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object dbname) {
        Object object;
        Object or__5238__auto__21614;
        Object dbs;
        Object object2 = dbs = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        dbs = null;
        Object object3 = or__5238__auto__21614 = RT.get((Object)object2, (Object)dbname);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__21614;
            or__5238__auto__21614 = null;
        } else {
            Object object4 = dbname;
            dbname = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke((Object)"Could not find ", object4, (Object)" in catalog"));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$connect_local_database.invokeStatic(object2);
    }
}

