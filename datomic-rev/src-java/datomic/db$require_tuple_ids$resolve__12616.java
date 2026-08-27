/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$require_tuple_ids$resolve__12616
extends AFunction {
    Object tup;
    Object db;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"string-tempid?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"not-an-entity");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__6 = RT.keyword(null, (String)"tuple");

    public db$require_tuple_ids$resolve__12616(Object object, Object object2) {
        this.tup = object;
        this.db = object2;
    }

    public Object invoke(Object id) {
        Object object;
        if (Util.identical((Object)id, null)) {
            object = id;
            id = null;
        } else {
            Object or__5238__auto__12618;
            Object object2 = ((IFn)const__1.getRawRoot()).invoke(id);
            Object object3 = or__5238__auto__12618 = object2 != null && object2 != Boolean.FALSE ? id : ((IFn)const__2.getRawRoot()).invoke(this_.db, id);
            if (object3 != null && object3 != Boolean.FALSE) {
                object = or__5238__auto__12618;
                or__5238__auto__12618 = null;
            } else {
                Object object4 = id;
                id = null;
                db$require_tuple_ids$resolve__12616 this_ = null;
                object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke((Object)"Unable to resolve entity: ", object4, (Object)" in tuple"), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__6, this_.tup}));
            }
        }
        return object;
    }
}

