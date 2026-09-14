/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$create_attr_pred$fn__13097
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"requiring-resolve!");

    public Object invoke(Object name) {
        Object object = name;
        Object object2 = name;
        name = null;
        return Tuple.create((Object)object, (Object)((IFn)const__0.getRawRoot()).invoke(object2));
    }
}

