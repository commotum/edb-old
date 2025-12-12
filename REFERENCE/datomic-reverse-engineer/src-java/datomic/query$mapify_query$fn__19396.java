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

public final class query$mapify_query$fn__19396
extends AFunction {
    Object query;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public query$mapify_query$fn__19396(Object object) {
        this.query = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.query);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}

