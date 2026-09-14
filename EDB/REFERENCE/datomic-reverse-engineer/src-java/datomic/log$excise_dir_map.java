/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$excise_dir_map$fn__16399;
import datomic.log$excise_dir_map$fn__16404;

public final class log$excise_dir_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object log2, Object ts) {
        Object object = log2;
        log2 = null;
        Object object2 = ts;
        ts = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$excise_dir_map$fn__16399(), (Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new log$excise_dir_map$fn__16404(object), object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$excise_dir_map.invokeStatic(object3, object4);
    }
}

