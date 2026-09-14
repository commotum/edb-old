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
import datomic.db$force_map_keywords$fn__13677;
import datomic.db$force_map_keywords$fn__13681;

public final class db$force_map_keywords
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object m) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)new db$force_map_keywords$fn__13677(), ((IFn)const__1.getRawRoot()).invoke(m));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = m;
            m = null;
        } else {
            Object object3 = db2;
            db2 = null;
            Object object4 = m;
            m = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new db$force_map_keywords$fn__13681(object3), (Object)PersistentArrayMap.EMPTY, object4);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$force_map_keywords.invokeStatic(object3, object4);
    }
}

