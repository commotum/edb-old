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
import datomic.Entity;

public final class query$touch
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");

    public static Object invokeStatic(Object e) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(e);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = e;
            e = null;
        } else {
            Object object3 = e;
            e = null;
            object = ((Entity)object3).touch();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$touch.invokeStatic(object2);
    }
}

