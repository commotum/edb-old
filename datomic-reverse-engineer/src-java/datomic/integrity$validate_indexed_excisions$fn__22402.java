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

public final class integrity$validate_indexed_excisions$fn__22402
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"validate-excision");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"flush");

    public Object invoke(Object spec) {
        ((IFn)const__0.getRawRoot()).invoke(spec);
        ((IFn)const__1.getRawRoot()).invoke((Object)".");
        ((IFn)const__2.getRawRoot()).invoke();
        Object var1_1 = null;
        return spec;
    }
}

