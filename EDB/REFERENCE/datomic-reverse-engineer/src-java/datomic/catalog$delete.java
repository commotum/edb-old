/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class catalog$delete
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"db-name->db-id");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__3 = RT.keyword((String)"datomic", (String)"deleted");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object catalog2, Object db_name) {
        Object object;
        Object temp__5455__auto__11130;
        Object object2 = temp__5455__auto__11130 = ((IFn)const__0.getRawRoot()).invoke(catalog2, db_name);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__11130;
            temp__5455__auto__11130 = null;
            Object dbid = object3;
            Object object4 = catalog2;
            catalog2 = null;
            Object object5 = dbid;
            dbid = null;
            Object object6 = db_name;
            db_name = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object4, (Object)const__3, ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), (Object)PersistentHashSet.EMPTY), object5), object6);
        } else {
            object = catalog2;
            Object object7 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$delete.invokeStatic(object3, object4);
    }
}

