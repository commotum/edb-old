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

public final class aws$defclient$fn__17395$fn__17396
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.pprint", (String)"pprint");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"type-descriptor");
    public static final Object const__2 = RT.classForName((String)"com.amazonaws.ClientConfiguration");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2));
        }
        finally {
            ((IFn)const__3.getRawRoot()).invoke();
        }
        return object;
    }
}

