/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class catalog$deleted_database_id_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"db-id");
    public static final Var const__3 = RT.var((String)"datomic.catalog", (String)"get-catalog");
    public static final Var const__4 = RT.var((String)"datomic.catalog", (String)"deleted?");

    public static Object invokeStatic(Object cluster2, Object db_id) {
        Object catalog2;
        Object temp__5455__auto__11156;
        Object object = db_id;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object2 = cluster2;
        cluster2 = null;
        Object object3 = temp__5455__auto__11156 = ((IFn)const__3.getRawRoot()).invoke(object2);
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)new RuntimeException("No catalog");
        }
        Object object4 = temp__5455__auto__11156;
        temp__5455__auto__11156 = null;
        Object object5 = catalog2 = object4;
        catalog2 = null;
        Object object6 = db_id;
        db_id = null;
        return ((IFn)const__4.getRawRoot()).invoke(object5, object6);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$deleted_database_id_QMARK_.invokeStatic(object3, object4);
    }
}

