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

public final class db$force_map_keywords$fn__13677
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"number?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keyword?");

    public Object invoke(Object k) {
        Object object;
        Object or__5238__auto__13679;
        Object object2 = or__5238__auto__13679 = ((IFn)const__0.getRawRoot()).invoke(k);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13679;
            or__5238__auto__13679 = null;
        } else {
            Object object3 = k;
            k = null;
            db$force_map_keywords$fn__13677 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        }
        return object;
    }
}

