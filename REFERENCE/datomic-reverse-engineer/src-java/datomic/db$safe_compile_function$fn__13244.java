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

public final class db$safe_compile_function$fn__13244
extends AFunction {
    Object code;
    public static final Var const__0 = RT.var((String)"datomic.janino", (String)"java-data-fn");

    public db$safe_compile_function$fn__13244(Object object) {
        this.code = object;
    }

    public Object invoke() {
        db$safe_compile_function$fn__13244 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.code);
    }
}

