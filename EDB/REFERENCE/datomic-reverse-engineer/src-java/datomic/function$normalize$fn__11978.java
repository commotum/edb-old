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

public final class function$normalize$fn__11978
extends AFunction {
    Object code;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public function$normalize$fn__11978(Object object) {
        this.code = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.code);
        }
        finally {
            ((IFn)const__1.getRawRoot()).invoke();
        }
        return object;
    }
}

