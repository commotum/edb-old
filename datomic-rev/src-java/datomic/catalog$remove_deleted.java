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

public final class catalog$remove_deleted
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__1 = RT.keyword((String)"datomic", (String)"deleted");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"disj");

    public static Object invokeStatic(Object catalog2, Object db_id) {
        Object object = catalog2;
        catalog2 = null;
        Object object2 = db_id;
        db_id = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), (Object)PersistentHashSet.EMPTY), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$remove_deleted.invokeStatic(object3, object4);
    }
}

