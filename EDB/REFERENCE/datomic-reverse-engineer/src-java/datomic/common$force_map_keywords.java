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
import datomic.common$force_map_keywords$fn__9212;

public final class common$force_map_keywords
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object m) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(m));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = m;
            m = null;
        } else {
            Object object3 = m;
            m = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)new common$force_map_keywords$fn__9212(), (Object)PersistentArrayMap.EMPTY, object3);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$force_map_keywords.invokeStatic(object2);
    }
}

