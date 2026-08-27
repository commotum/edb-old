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
import datomic.tools$serialized$fn__21730;

public final class tools$serialized
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"serialized");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"serializer");

    public static Object invokeStatic(Object f, Object agt) {
        Object object = agt;
        agt = null;
        Object object2 = f;
        f = null;
        return new tools$serialized$fn__21730(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$serialized.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$serialized.invokeStatic(object2);
    }
}

