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

public final class domain$lookup_with_object_cache$load_counter__16811
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"name");

    public Object invoke(Object ctr) {
        Object object = ctr;
        ctr = null;
        domain$lookup_with_object_cache$load_counter__16811 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), (Object)"-load"));
    }
}

