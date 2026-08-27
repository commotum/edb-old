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

public final class coordination$init_dev
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"data-dir");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"compare-and-set!");
    public static final Var const__4 = RT.var((String)"datomic.coordination", (String)"devspec");
    public static final Var const__5 = RT.var((String)"datomic.h2", (String)"init-tcp");

    public static Object invokeStatic(Object cluster_map, Object data_dir) {
        Object spec;
        Object object = cluster_map;
        cluster_map = null;
        Object object2 = data_dir;
        data_dir = null;
        Object object3 = spec = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, ((IFn)const__2.getRawRoot()).invoke(object2, (Object)"/db"));
        spec = null;
        return ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), null, ((IFn)const__5.getRawRoot()).invoke(object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return coordination$init_dev.invokeStatic(object3, object4);
    }
}

