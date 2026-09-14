/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.aws;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$de_item_map$fn__20437
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"datomic.core2.aws.ddb", (String)"de-attribute-value");

    public Object invoke(Object m, Object k, Object v) {
        Object object = m;
        m = null;
        Object object2 = k;
        k = null;
        Object object3 = v;
        v = null;
        ddb$de_item_map$fn__20437 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__1.getRawRoot()).invoke(object3));
    }
}

