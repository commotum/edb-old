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
import datomic.common$map_from_env$fn__9050;

public final class common$map_from_env
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object env) {
        common$map_from_env$fn__9050 common$map_from_env$fn__9050 = new common$map_from_env$fn__9050(env);
        Object object = env;
        env = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)common$map_from_env$fn__9050, (Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$map_from_env.invokeStatic(object2);
    }
}

