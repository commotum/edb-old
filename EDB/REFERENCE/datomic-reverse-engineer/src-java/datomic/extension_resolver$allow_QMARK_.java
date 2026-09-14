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

public final class extension_resolver$allow_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"preds-ref");

    public static Object invokeStatic(Object sym, Object pred2) {
        Object object = pred2;
        pred2 = null;
        Object object2 = sym;
        sym = null;
        return ((IFn)((IFn)object).invoke(((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot()))).invoke(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extension_resolver$allow_QMARK_.invokeStatic(object3, object4);
    }
}

