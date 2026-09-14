/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class catalog$db_name__GT_db_id
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Keyword const__1 = RT.keyword(null, (String)"db-id");

    public static Object invokeStatic(Object catalog2, Object db_name) {
        Object object = catalog2;
        catalog2 = null;
        Object object2 = db_name;
        db_name = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Tuple.create((Object)object2, (Object)const__1));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$db_name__GT_db_id.invokeStatic(object3, object4);
    }
}

