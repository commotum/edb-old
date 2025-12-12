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

public final class datalog$rule_map$fn__18758
extends AFunction {
    Object rules;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public datalog$rule_map$fn__18758(Object object) {
        this.rules = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.rules);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}

