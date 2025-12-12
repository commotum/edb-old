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

public final class common$env_key__GT_clj_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"lower-case");

    public static Object invokeStatic(Object k) {
        Object object = k;
        k = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), (Object)"_", (Object)"-"));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$env_key__GT_clj_key.invokeStatic(object2);
    }
}

