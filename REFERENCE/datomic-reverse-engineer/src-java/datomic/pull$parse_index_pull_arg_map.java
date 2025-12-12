/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class pull$parse_index_pull_arg_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.edn", (String)"read-string");

    public static Object invokeStatic(Object arg_map) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(arg_map);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = arg_map;
            arg_map = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        } else {
            object = arg_map;
            Object object4 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$parse_index_pull_arg_map.invokeStatic(object2);
    }
}

