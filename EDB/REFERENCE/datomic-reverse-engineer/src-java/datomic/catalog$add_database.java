/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class catalog$add_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"db-name");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"db-id");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"db-id");
    public static final Keyword const__7 = RT.keyword((String)"datomic", (String)"deleted");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"disj");

    public static Object invokeStatic(Object catalog2, Object db_name, Object db_id) {
        Object object = db_name;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object2 = db_id;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__3))));
        }
        Object object3 = catalog2;
        catalog2 = null;
        Object object4 = db_name;
        db_name = null;
        Object[] objectArray = new Object[]{const__6, db_id};
        Object object5 = db_id;
        db_id = null;
        return ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object3, object4, (Object)RT.mapUniqueKeys((Object[])objectArray)), (Object)const__7, ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), (Object)PersistentHashSet.EMPTY), object5);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return catalog$add_database.invokeStatic(object4, object5, object6);
    }
}

