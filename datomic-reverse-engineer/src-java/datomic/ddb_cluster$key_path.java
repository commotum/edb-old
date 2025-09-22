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

public final class ddb_cluster$key_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster", (String)"path");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"key");

    public static Object invokeStatic(Object path_map, Object k) {
        Object object = path_map;
        path_map = null;
        Object object2 = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2, object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb_cluster$key_path.invokeStatic(object3, object4);
    }
}

