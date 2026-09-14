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

public final class promise$settable_future$reify__10384$fn__10385
extends AFunction {
    Object v;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public promise$settable_future$reify__10384$fn__10385(Object object) {
        this.v = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.v));
        }
        finally {
            ((IFn)const__2.getRawRoot()).invoke();
        }
        return object;
    }
}

