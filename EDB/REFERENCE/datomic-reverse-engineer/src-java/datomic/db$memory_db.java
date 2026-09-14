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

public final class db$memory_db
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"index");
    public static final Keyword const__2 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__3 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__4 = RT.keyword(null, (String)"history");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, null, (Object)const__2, null, (Object)const__3, null, (Object)const__4, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$memory_db.invokeStatic(object2);
    }
}

