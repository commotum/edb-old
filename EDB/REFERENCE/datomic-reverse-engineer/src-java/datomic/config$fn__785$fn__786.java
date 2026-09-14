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

public final class config$fn__785$fn__786
extends AFunction {
    Object n;
    public static final Var const__0 = RT.var((String)"clojure.edn", (String)"read-string");

    public config$fn__785$fn__786(Object object) {
        this.n = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.n = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.n);
        }
        catch (RuntimeException _) {
            object = null;
        }
        return object;
    }
}

