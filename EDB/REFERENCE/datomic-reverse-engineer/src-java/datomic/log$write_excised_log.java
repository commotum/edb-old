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
import datomic.log$write_excised_log$fn__16409;

public final class log$write_excised_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object cs, Object lookup, Object xpreds, Object ts, Object dir_map) {
        Object object = ts;
        ts = null;
        Object object2 = lookup;
        lookup = null;
        Object object3 = xpreds;
        xpreds = null;
        Object object4 = cs;
        cs = null;
        Object object5 = dir_map;
        dir_map = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$write_excised_log$fn__16409(object, object2, object3, object4), (Object)PersistentArrayMap.EMPTY, object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return log$write_excised_log.invokeStatic(object6, object7, object8, object9, object10);
    }
}

