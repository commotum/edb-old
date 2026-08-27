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
import datomic.ddb$fn__17437$fn__17439;

public final class ddb$fn__17437
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object _, Object _2, Object val, Object _3) {
        Object object = val;
        val = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new ddb$fn__17437$fn__17439(), (Object)PersistentArrayMap.EMPTY, object);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return ddb$fn__17437.invokeStatic(object5, object6, object7, object8);
    }
}

