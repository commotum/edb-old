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

public final class cli$apply_defaults
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Keyword const__1 = RT.keyword(null, (String)"long-name");
    public static final Keyword const__2 = RT.keyword(null, (String)"default");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"merge");

    public static Object invokeStatic(Object m, Object spec) {
        Object defaults;
        Object object = spec;
        spec = null;
        Object object2 = defaults = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, (Object)const__2);
        defaults = null;
        Object object3 = m;
        m = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cli$apply_defaults.invokeStatic(object3, object4);
    }
}

