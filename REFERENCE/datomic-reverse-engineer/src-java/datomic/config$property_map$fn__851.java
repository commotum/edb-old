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

public final class config$property_map$fn__851
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object k, Object v) {
        Object object;
        Object temp__5455__auto__853;
        Object object2 = v;
        v = null;
        Object object3 = temp__5455__auto__853 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__853;
            temp__5455__auto__853 = null;
            Object v2 = object4;
            Object object5 = m;
            m = null;
            Object object6 = k;
            k = null;
            Object object7 = v2;
            v2 = null;
            config$property_map$fn__851 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object5, object6, object7);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

