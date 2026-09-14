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

public final class treewalk$lookup_val
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");

    public static Object invokeStatic(Object lookup, Object id, Object allow_missing_QMARK_) {
        Object object;
        Object object2 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = lookup;
            lookup = null;
            Object object4 = id;
            id = null;
            object = RT.get((Object)object3, (Object)object4);
        } else {
            Object object5 = lookup;
            lookup = null;
            Object object6 = id;
            id = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object5, object6);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$lookup_val.invokeStatic(object4, object5, object6);
    }
}

