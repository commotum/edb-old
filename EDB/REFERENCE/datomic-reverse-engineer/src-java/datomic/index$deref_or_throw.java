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

public final class index$deref_or_throw
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object ref) {
        Object object = ref;
        ref = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(object);
        if (result2 instanceof Throwable) {
            Object object2 = result2;
            result2 = null;
            throw (Throwable)object2;
        }
        Object var1_1 = null;
        return result2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$deref_or_throw.invokeStatic(object2);
    }
}

