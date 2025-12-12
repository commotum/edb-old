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

public final class db$bootstrap_db
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"bootstrap-db");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"squuid");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"base-bootstrap-db");
    public static final Keyword const__6 = RT.keyword(null, (String)"id");

    public static Object invokeStatic(Object id) {
        Object object = id;
        id = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot()), (Object)const__6, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$bootstrap_db.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke()));
    }

    public Object invoke() {
        return db$bootstrap_db.invokeStatic();
    }
}

