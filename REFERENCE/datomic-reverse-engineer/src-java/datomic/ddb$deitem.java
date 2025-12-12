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
import datomic.ddb$deitem$fn__17714;

public final class ddb$deitem
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new ddb$deitem$fn__17714(), (Object)PersistentArrayMap.EMPTY, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$deitem.invokeStatic(object2);
    }
}

