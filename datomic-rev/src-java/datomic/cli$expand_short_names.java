/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cli$expand_short_names
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Keyword const__1 = RT.keyword(null, (String)"short-name");
    public static final Keyword const__2 = RT.keyword(null, (String)"long-name");
    public static final Var const__3 = RT.var((String)"clojure.set", (String)"rename-keys");

    public static Object invokeStatic(Object m, Object spec) {
        Object object = spec;
        spec = null;
        Object idx = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, (Object)const__2);
        Object object2 = m;
        m = null;
        Object object3 = idx;
        idx = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cli$expand_short_names.invokeStatic(object3, object4);
    }
}

