/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IDeref
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$maybe_deref
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object v) {
        Object object;
        Object G__9262 = v;
        Object object2 = v;
        v = null;
        if (object2 instanceof IDeref) {
            Object object3 = G__9262;
            G__9262 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3);
        } else {
            object = G__9262;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$maybe_deref.invokeStatic(object2);
    }
}

