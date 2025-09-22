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
import datomic.common$mapk$fn__9157;

public final class common$mapk
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object f, Object coll) {
        Object object = f;
        f = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new common$mapk$fn__9157(object), (Object)PersistentArrayMap.EMPTY, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$mapk.invokeStatic(object3, object4);
    }
}

