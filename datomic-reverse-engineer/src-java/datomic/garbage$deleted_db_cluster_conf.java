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

public final class garbage$deleted_db_cluster_conf
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"db-id");

    public static Object invokeStatic(Object uri2, Object id) {
        Object object = uri2;
        uri2 = null;
        Object object2 = id;
        id = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), (Object)const__3), (Object)const__4, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$deleted_db_cluster_conf.invokeStatic(object3, object4);
    }
}

